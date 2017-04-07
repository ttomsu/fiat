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
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.netflix.spinnaker.fiat.model.Authorization;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
public class Permissions {

  public static Permissions EMPTY = new Permissions.Builder().build();
  private static Set<Authorization> UNRESTRICTED_AUTH = ImmutableSet.copyOf(Authorization.values());

  private final Map<Authorization, List<String>> permissions;

  private Permissions(Map<Authorization, List<String>> p) {
    this.permissions = p;
  }

  @JsonCreator
  public static Permissions factory(Map<Authorization, List<String>> data) {
    return new Builder().set(data).build();
  }

  @JsonValue
  private Map<Authorization, List<String>> getPermissions() {
    return permissions;
  }

  public List<String> allGroups() {
    return permissions.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
  }

  @VisibleForTesting
  protected List<String> get(Authorization a) {
    return permissions.get(a);
  }

  public boolean isEmpty() {
    return getPermissions().isEmpty();
  }

  public boolean isRestricted() {
    return this.getPermissions().values().stream().anyMatch(groups -> !groups.isEmpty());
  }

  public boolean isAuthorized(Set<Role> userRoles) {
    return !getAuthorizations(userRoles).isEmpty();
  }

  public Set<Authorization> getAuthorizations(Set<Role> userRoles) {
    val r = userRoles.stream().map(Role::getName).collect(Collectors.toList());
    return getAuthorizations(r);
  }

  public Set<Authorization> getAuthorizations(List<String> userRoles) {
    if (isEmpty()) {
      return UNRESTRICTED_AUTH;
    }

    return this.getPermissions()
               .entrySet()
               .stream()
               .filter(entry -> !Collections.disjoint(entry.getValue(), userRoles))
               .map(Map.Entry::getKey)
               .collect(Collectors.toSet());
  }


  public static class Builder extends LinkedHashMap<Authorization, List<String>> {

    @JsonCreator
    public static Builder factory(Map<Authorization, List<String>> data) {
      return new Builder().set(data);
    }

    public Builder set(Map<Authorization, List<String>> p) {
      this.clear();
      this.putAll(p);
      return this;
    }

    public Builder add(Authorization a, String group) {
      this.computeIfAbsent(a, ignored -> new ArrayList<>()).add(group);
      return this;
    }

    public Builder add(Authorization a, List<String> groups) {
      groups.forEach(group -> add(a, group));
      return this;
    }

    public Permissions build() {
      ImmutableMap.Builder<Authorization, List<String>> builder = ImmutableMap.builder();
      this.forEach((auth, groups) -> {
        List<String> lowerGroups = groups.stream()
                                         .map(String::trim)
                                         .map(String::toLowerCase)
                                         .collect(Collectors.toList());
        builder.put(auth, ImmutableList.copyOf(lowerGroups));
      });
      return new Permissions(builder.build());
    }
  }
}
