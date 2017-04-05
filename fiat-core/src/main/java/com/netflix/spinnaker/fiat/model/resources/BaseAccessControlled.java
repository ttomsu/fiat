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

package com.netflix.spinnaker.fiat.model.resources;

import com.netflix.spinnaker.fiat.model.Authorization;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
abstract class BaseAccessControlled<R extends Resource.AccessControlled> implements Resource.AccessControlled<R> {

  /**
   * Legacy holdover where setting `requiredGroupMembership` implied both read and write
   * permissions.
   */
  @SuppressWarnings("unchecked")
  public <T extends BaseAccessControlled> T setRequiredGroupMembership(List<String> membership) {
    if (membership == null || membership.isEmpty()) {
      return (T) this;
    }

    if (!getPermissions().isEmpty()) {
      log.warn("`requiredGroupMembership` found on resource `" + getName() +
                   "` and ignored because `permissions` are present");
      return (T) this;
    }

    log.warn("Deprecated `requiredGroupMembership` found on resource `" + getName() + "`. " +
                 "Please update to `permissions`.");
    membership.stream()
              .map(group -> group.trim().toLowerCase())
              .forEach(group -> getPermissions().add(Authorization.READ, group)
                                                .add(Authorization.WRITE, group));
    return (T) this;
  }
}
