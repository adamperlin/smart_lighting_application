package com.nanosoft.sample.smartlightingcamera;

import android.util.Log;

import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Vertex;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import java.util.Collections;
import java.util.HashSet;

class ResultsProcessor {
    private JSONObject response;
    private HashSet<String> labelsOfInterest;

    // Update with relevant labels
    private String[] labels = {
            "Laptop",
            "Technology",
            "Electronic Device"
    };

    private class NormalizedVertex {
        double x, y;
        NormalizedVertex(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private class BoundingBox {
        double topY, rightX;
        BoundingBox(double rightX, double topY) {
            this.topY = topY;
            this.rightX = rightX;
        }
    }

    ResultsProcessor(JSONObject response) {
        this.response = response;

        labelsOfInterest = new HashSet<>();
        Collections.addAll(labelsOfInterest, labels);
    }

    void processResults() throws JSONException  {
     //  List<AnnotateImageResponse> responses = response.getResponsesList();
       double maxScore = -1.0;
       String maxScoreLabel = "";

       /*for (AnnotateImageResponse ar : responses) {
           for ( entity : ar.getLocalizedObjectAnnotationsList()) {
                double score = entity.getScore();
                if (score > maxScore) {
                    maxScore = score;
                    maxScoreLabel = entity.getName();
                    maxScoreBoundingPoly = entity.getBoundingPoly();
                }
           }
       }*/

       JSONArray responses = response.getJSONArray("responses");
       if (responses.length() < 1) {
           throw new JSONException("request array length 0");
       }

       JSONObject firstResponse = responses.getJSONObject(0);
       JSONArray localizedObjectAnnotations = firstResponse.getJSONArray("localizedObjectAnnotations");

       BoundingBox maxScoreBoundingBox = null;
       for (int i = 0; i < localizedObjectAnnotations.length(); i++) {
           JSONObject currentObj = localizedObjectAnnotations.getJSONObject(i);
           String name = currentObj.getString("name");
           Log.d("Label: ", name);
           double score = currentObj.getDouble("score");

           if (score > maxScore) {
               JSONObject boundingPoly = currentObj.getJSONObject("boundingPoly");
               JSONArray normalizedVerticesJson = boundingPoly.getJSONArray("normalizedVertices");

               NormalizedVertex[] normalizedVertices = extractNormalizedVertices(normalizedVerticesJson);
               maxScoreBoundingBox = computeBoundingBox(normalizedVertices);
               maxScoreLabel = name;
           }
       }

       /*

       List<AnnotateImageResponse> responses = response.getResponses();
       if (labels != null) {
           for (AnnotateImageResponse resp : responses) {
               if (resp != null) {

               }
               for (EntityAnnotation label : resp.getLabelAnnotations()) {
                   String labelDesc = label.getDescription();
                   Log.d("Label desc: ", labelDesc);
                   double score = label.getScore();
                   BoundingPoly bp = label.getBoundingPoly();
                   if (bp != null) {
                       Log.d("Bounding Vertices", "");
                       for (Vertex v : bp.getVertices()) {
                           Log.d("Bounding Vertex", bp.toString());
                       }
                   }
                   if (score > maxScore) {
                       maxScore = score;
                       maxScoreLabel = labelDesc;
                       maxScoreBoundingPoly = label.getBoundingPoly();
                   }
               }
           }
       }*/

       if (!maxScoreLabel.isEmpty()) {
           String key = sanitize(maxScoreLabel);
           if (labelsOfInterest.contains(key)) {
               // Do something....
               Log.d("FOUND", "Label of interest: " + key);

               if (maxScoreBoundingBox != null) {
                   Log.d("MAX SCORE BOUNDING BOX", String.valueOf(maxScoreBoundingBox.rightX) + " " + String.valueOf(maxScoreBoundingBox.topY));
               }
           }
       } else {
           Log.d("NOT FOUND","No maxScoreLabel");
       }
    }

    private String sanitize(String s) {
        return s.trim();
    }

    private NormalizedVertex[] extractNormalizedVertices(JSONArray normalizedVertices) throws JSONException {
        NormalizedVertex[] vertices = new NormalizedVertex[normalizedVertices.length()];
        for (int v = 0; v < normalizedVertices.length(); v++) {
            JSONObject currentVertex = normalizedVertices.getJSONObject(v);
            if (currentVertex.has("x") && currentVertex.has("y")) {
                double x = currentVertex.getDouble("x");
                double y = currentVertex.getDouble("y");
                NormalizedVertex vert = new NormalizedVertex(x, y);
                vertices[v] = vert;
            }
        }

        return vertices;
    }

    private BoundingBox computeBoundingBox(NormalizedVertex[] vertices) {
        double maxY = 0.0;
        double maxX = 0.0;

        for (NormalizedVertex v : vertices) {
            if (v != null) {
                if (v.x > maxX) {
                    maxX = v.x;
                }

                if (v.y > maxY) {
                    maxY = v.y;
                }
            }
        }
        return new BoundingBox(maxX, maxY);
    }
}
