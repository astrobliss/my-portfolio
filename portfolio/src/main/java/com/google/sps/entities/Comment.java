package com.google.sps.entities;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
/**
 * This is a POJO class meant to store all information required to display a datastore comment object
 * from the DataServlet on one of the portfolio webpages
 * Serialized intended to be done using Gson
 */
public class Comment {
  private String authorName;
  private String commentText;
  private long timestampMs;

}
