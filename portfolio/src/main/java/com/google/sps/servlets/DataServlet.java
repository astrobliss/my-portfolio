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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.entities.Comment;
import com.google.sps.utils.Requests;
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
  private static final UserService userService = UserServiceFactory.getUserService();
  private static Gson gson = new Gson();

  /**
   * Gives response containing a single Json List with all stored comments from datastore
   * List sorted from oldest to newest comment
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp-ms", SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(query);

    List<Comment> comments = new ArrayList<>();
    for (Entity commentEn : results.asIterable()) {
      String authorId = (String) commentEn.getProperty("author-id");
      // TODO query all user entities at once
      Entity authorEn = UserInformationServlet.getUserEntity(authorId);
      if(authorEn != null) {
        comments.add(Comment.builder()
            .commentText((String) commentEn.getProperty("comment-text"))
            .authorName((String) authorEn.getProperty("display-name"))
            .timestampMs((long) commentEn.getProperty("timestamp-ms"))
            .build()
        );
      }
    }
    String json = gson.toJson(comments);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /**
   * Iff request has a non-null comment-text parameter,
   *   then create and store the parameter's String as a Datastore Comment Object
   * Always redirect to index.html
   * If poster not logged in, return a HTTP 403 error code as they must be logged in to comment
   * If comment contains no text return a HTTP 400 error code as empty comments aren't allowed
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if(userService.isUserLoggedIn()) {
      String comment = Requests.getParameter(request, "comment-text", "");
      if ("".equals(comment)) {
        response.setStatus(400);
      } else {
        // User is logged in and posting a non-empty comment, store their requested comment
        String authorId = userService.getCurrentUser().getUserId();
        long timestampMs = System.currentTimeMillis();
        Entity commentEn = new Entity("Comment");
        commentEn.setProperty("comment-text", comment);
        commentEn.setProperty("timestamp-ms", timestampMs);
        commentEn.setProperty("author-id", authorId);
        datastore.put(commentEn);
      }
    } else {
      response.setStatus(403);
    }
    response.sendRedirect("/index.html");
  }
}
