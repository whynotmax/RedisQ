package dev.mzcy.queue.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class QueueModel {

    String name;
    String displayName;
    String server;
    String npcId;

    long sendIntervalSeconds;
    long maxPlayersInQueue;

}
