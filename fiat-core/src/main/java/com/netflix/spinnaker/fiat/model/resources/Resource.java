/*
 * Copyright 2016 Google, Inc.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netflix.spinnaker.fiat.model.Authorization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface Resource {
  String getName();

  @JsonIgnore
  ResourceType getResourceType();

  /**
   * Represents Resources that have restrictions on permissions.
   */
  interface AccessControlled<R extends Resource.AccessControlled> extends Resource, Authorizable {

    /**
     * Grant the following Authorizations to this resource. This should not be done to shared
     * instances of this object. Instead, use the {@link} cloneWithoutAuthorizations} method to get
     * a new copy first.
     */
    R setAuthorizations(Set<Authorization> authorizations);

    /**
     * @return Implementations should return a modified copy of the object, in order to prevent
     * inadvertent authorization leaks.
     */
    R cloneWithoutAuthorizations();

    Permissions getPermissions();
  }

  /**
   * Representation of authorization configuration for a resource. This should be defined on the
   * account/application config like:
   *
   * name: resourceName
   * permissions:
   *   read:
   *   - role1
   *   - role2
   *   write:
   *   - role1
   *
   * Group names are trimmed of whitespace and lowercased.
   */
  class Permissions extends HashMap<Authorization, List<String>> {

    public Permissions() {
      super();
    }

    public Permissions add(Authorization a, String role) {
      this.computeIfAbsent(a, ignored -> new ArrayList<>()).add(role.trim().toLowerCase());
      return this;
    }

    public Set<Authorization> getAuthorizations(List<String> userRoles) {
      return this.entrySet()
                 .stream()
                 .filter(entry -> !Collections.disjoint(entry.getValue(), userRoles))
                 .map(Entry::getKey)
                 .collect(Collectors.toSet());
    }

    public boolean isRestricted() {
      return this.values().stream().anyMatch(groups -> !groups.isEmpty());
    }
  }
}
