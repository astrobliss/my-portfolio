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

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class FindMeetingQuery {
  /**
   * @param events a collection of events that may or may not include people in the meeting request
   * @param request a meeting request specifying who should meet and for how long
   * @return all TimeRanges where a meetingRequest could be scheduled
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Set<String> requestedAttendees = new HashSet<>(request.getAttendees());
    List<TimeRange> allConflictingEvents = events.stream()
        .filter(event -> isConflictingEvent(event, requestedAttendees))
        .map(Event::getWhen)
        .sorted(TimeRange.ORDER_BY_START)
        .collect(Collectors.toList());
    Collection<TimeRange> allAvailableTimes = new ArrayList<>();
    int currentTime = TimeRange.START_OF_DAY;
    long requiredDuration = request.getDuration();

    // Add all meeting times that end at a conflicting event
    for (TimeRange nextConflict : allConflictingEvents) {
      if (currentTime >= TimeRange.END_OF_DAY) {
        break; // No more scheduling possible
      }
      if (nextConflict.start() - currentTime >= requiredDuration) {
        allAvailableTimes.add(TimeRange.fromStartEnd(currentTime, nextConflict.start(), false));
      }
      currentTime = Math.max(currentTime, nextConflict.end());
    }

    // If there is enough time for a meeting after all conflicts have passed, add it
    if (TimeRange.END_OF_DAY - currentTime >= requiredDuration) {
      allAvailableTimes.add(TimeRange.fromStartEnd(currentTime, TimeRange.END_OF_DAY, true));
    }
    return allAvailableTimes;
  }

  /**
   * @return true iff an event occurs before the day ends
   */
  private boolean isEventDuringDay(Event event) {
    return event.getWhen().start() <= TimeRange.END_OF_DAY;
  }

  /**
   * @param people a set of Strings, each representing a person
   * @return true iff at least one person attends the event
   */
  private boolean isAnyoneAttendingEvent(Event event, Set<String> people) {
    if (event.getAttendees().size() <= people.size()) {
      return event.getAttendees().stream().anyMatch(people::contains);
    } else {
      return people.stream().anyMatch(event.getAttendees()::contains);
    }
  }

  /**
   * Determines if a given event would be a conflict for a potential meeting between meetingAttendees
   * @param meetingAttendees a set of all requested Attendees of a Meeting
   * @return true iff at least one meetingAttendee attends the event and the event is during the day
   */
  private boolean isConflictingEvent(Event event, Set<String> meetingAttendees) {
    return (isEventDuringDay(event) && isAnyoneAttendingEvent(event,meetingAttendees));
  }
}
