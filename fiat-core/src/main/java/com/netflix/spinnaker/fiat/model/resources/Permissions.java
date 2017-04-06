/*
 * Copyright 2017 Netflix, Inc.
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

package com.netflix.spinnaker.fiat.model.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netflix.spinnaker.fiat.model.Authorization;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface Permissions {
  
  @JsonIgnore
  Map<Authorization, List<String>> getPermissions();
  
  List<String> get(Authorization a);
  
  default boolean isEmpty() {
    return getPermissions().isEmpty();
  }
  
  default boolean isRestricted() {
    return this.getPermissions().values().stream().anyMatch(groups -> !groups.isEmpty());
  }

  default Set<Authorization> getAuthorizations(List<String> userRoles) {
    return this.getPermissions()
               .entrySet()
               .stream()
               .filter(entry -> !Collections.disjoint(entry.getValue(), userRoles))
               .map(Map.Entry::getKey)
               .collect(Collectors.toSet());
  }

  @NoArgsConstructor
  class Mutable extends LinkedHashMap<Authorization, List<String>> implements Permissions {
    @Override
    public List<String> get(Authorization a) {
      return this.computeIfAbsent(a, z -> new ArrayList<>());
    }

    @Override
    public Map<Authorization, List<String>> getPermissions() {
      return this;
    }

    public Mutable add(Authorization a, String group) {
      get(a).add(group);
      return this;
    }

    public Permissions immutable() {
      ImmutableMap.Builder<Authorization, List<String>> builder = ImmutableMap.builder();
      this.forEach((auth, groups) -> builder.put(auth, ImmutableList.copyOf(groups)));
      return new Immutable(builder.build());
    }
  }

  class Immutable implements Permissions {
    Map<Authorization, List<String>> permissions;

    @JsonCreator
    public Immutable(Map<Authorization, List<String>> p) {
      this.permissions = p;
    }

    @JsonValue
    public Map<Authorization, List<String>> getPermissions() {
      return this.permissions;
    }

    @Override
    public List<String> get(Authorization a) {
      return permissions.get(a);
    }
  }
}
