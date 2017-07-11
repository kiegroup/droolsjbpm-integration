/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class KjarResource {

	private String resource;
	private ResourceTypeEnum resourceType;

	public KjarResource() {
	}

	public KjarResource(String resource, ResourceTypeEnum resourceType) {
		this.resource = resource;
		this.resourceType = resourceType;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public ResourceTypeEnum getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceTypeEnum resourceType) {
		this.resourceType = resourceType;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(resource).append(resourceType).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(resource).append(resourceType).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		KjarResource rhs = (KjarResource) obj;
		return new EqualsBuilder().append(resource, rhs.resource).append(resourceType, rhs.resourceType)
				.isEquals();
	}

}
