// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Uses Datastore to store Comment Objects which can be Read on GET or Added to on POST
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static Gson gson = new Gson();

  /**
   * Gives response containing a single Json List with all stored comments from datastore
   * List sorted from oldest to newest comment
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(query);

    List<String> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      comments.add((String) entity.getProperty("text"));
    }
    String json = gson.toJson(comments);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /**
   * Iff request has a non-null comment-text parameter,
   *   then create and store the parameter's String as a Datastore Comment Object
   * Always redirect to index.html
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter("comment-text");
    if(comment != null){
      long timestamp = System.currentTimeMillis();
      Entity commentEn = new Entity("Comment");
      commentEn.setProperty("text",comment);
      commentEn.setProperty("timestamp",timestamp);
      datastore.put(commentEn);
    }
    response.sendRedirect("/index.html");
  }
}
