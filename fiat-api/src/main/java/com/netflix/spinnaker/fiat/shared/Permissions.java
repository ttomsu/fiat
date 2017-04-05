/*
 * Copyright 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.fiat.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netflix.spinnaker.fiat.model.Authorization;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Permissions {

  private Map<Authorization, List<String>> permissions;

  @JsonCreator
  public Permissions(Map<Authorization, List<String>> p) {
    this.permissions = p;
  }

  public List<String> get(Authorization a) {
    return permissions.computeIfAbsent(a, ignored -> new ArrayList<>());
  }

  public void add(Authorization a, String group) {
    permissions.get(a).add(group);
  }

  public boolean isEmpty() {
    return permissions.isEmpty();
  }

  @JsonValue
  public Map<Authorization, List<String>> getPermissions() {
    ImmutableMap.Builder<Authorization, List<String>> builder = ImmutableMap.builder();
    permissions.forEach((auth, groups) -> builder.put(auth, ImmutableList.copyOf(groups)));
    return builder.build();
  }

  public void setPermissions(Map<Authorization, List<String>> permissions) {
    this.permissions = permissions;
  }

  Permissions asImmutable() {
    return new Permissions(getPermissions());
  }
}
