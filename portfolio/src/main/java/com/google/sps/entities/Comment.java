package com.google.sps.entities;

import lombok.*;

@Builder
@Getter
@Setter
public class Comment {
  private String authorName;
  private String commentText;
  private long timestampMs;

}
