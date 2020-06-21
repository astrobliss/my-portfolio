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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Uses UserService and DataStore to allow Creation, Reading, and Updating of User objects
 * JSON is sent on GET, on POST create a new user object or update individual parameters
 */
@WebServlet("/userInfo")
public class UserInformationServlet extends HttpServlet {
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static Gson gson = new Gson();
  private static final List<String> userPropertyNames = Arrays.asList("displayName", "email");
  private static final UserService userService = UserServiceFactory.getUserService();

  /**
   * Sends JSON representation of the User with id user-id
   * If user-id is not present, send the logged in user's id
   * If no user-id is given and no user is logged in, return a HTTP 400 error code
   * If no data is found for the given user, return a HTTP 404 error code
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String currentUserId = null;
    if(userService.isUserLoggedIn()){
      currentUserId = userService.getCurrentUser().getUserId();
    }
    String requestedUserId = getRequestParameter("user-id", request, currentUserId);
    if(requestedUserId == null){
      response.setStatus(400);
      return;
    }
    Entity requestedUserEntity = getUserEntity(requestedUserId);
    if(requestedUserEntity == null) {
      response.setStatus(404);
      return;
    }
    String requestedUserJson = gson.toJson(requestedUserEntity.getProperties());
    response.setContentType("application/json;");
    response.getWriter().println(requestedUserJson);
  }


  /**
   * Deletes User Information for the logged in user
   * If no user logged in, return a HTTP 400 error code
   * If no User Information present, return a HTTP 404 error code
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if(!userService.isUserLoggedIn()){
      response.setStatus(400);
      return;
    }
    String userId = userService.getCurrentUser().getUserId();
    Entity userEntity = getUserEntity(userId);
    if(userEntity != null) {
      datastore.delete(userEntity.getKey());
    } else {
      response.setStatus(404);
    }
  }

  /**
   * Creates or Updates the current user's User Object with parameters given
   * If User isn't logged in return a HTTP 400 error code, Datastore is not modified
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if(!userService.isUserLoggedIn()) {
      response.setStatus(400);
      response.sendRedirect("/");
      return;
    }
    String userId = userService.getCurrentUser().getUserId();
    Entity userEntity = getUserEntity(userId);

    if(userEntity == null) {
      // Create and store a new entity
      userEntity = new Entity("User", userId);
      String userEmail = userService.getCurrentUser().getEmail();
      for(String propertyName: userPropertyNames) {
        String propertyValue = getRequestParameter(propertyName, request, "");
        userEntity.setProperty(propertyName, propertyValue);
      }
      userEntity.setProperty("id", userId);
      userEntity.setProperty("email", userEmail); // email shouldn't be changed on create
    } else {
      // Update present parameters
      for(String propertyName: userPropertyNames) {
        String currentPropertyValue = "";
        if(userEntity.hasProperty(propertyName)){
          currentPropertyValue = (String) userEntity.getProperty(propertyName);
        }
        String updatedPropertyValue = getRequestParameter(propertyName, request, currentPropertyValue);
        userEntity.setProperty(propertyName, updatedPropertyValue);
      }
    }
    datastore.put(userEntity);
    response.sendRedirect("/");
  }

  /**
   * Queries Datastore for a User Entity with the given Id, returns null if no match
   */
  private Entity getUserEntity(String userId) {
    Query query =
        new Query("User")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, userId));
    PreparedQuery results = datastore.prepare(query);
    return results.asSingleEntity();
  }

  /**
   * Gets a parameter from a HTTP request
   * Return defaultValue for missing parameter
   */
  private String getRequestParameter(String parameterName, HttpServletRequest request, String defaultValue) {
    String parameterValue = request.getParameter(parameterName);
    if(parameterValue == null) {
        return defaultValue;
    } else {
      return parameterValue;
    }
  }
}
