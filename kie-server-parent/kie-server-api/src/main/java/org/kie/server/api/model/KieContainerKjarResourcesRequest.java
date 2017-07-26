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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XmlRootElement(name = "kie-container-and-kjar-resources")
@XStreamAlias("kie-container-and-kjar-resources")
public class KieContainerKjarResourcesRequest {

	private KieContainerResource kieContainerResource;

	@XmlElementWrapper(name = "kjarResources")
	@XmlElement(name="kjarResource")
	@XStreamAlias("kjar-resources")
	private List<KjarResource> kjarResources = new ArrayList<>();

	public KieContainerResource getKieContainerResource() {
		return kieContainerResource;
	}

	public void setKieContainerResource(KieContainerResource kieContainerResource) {
		this.kieContainerResource = kieContainerResource;
	}

	public Collection<KjarResource> getKjarResources() {
		return kjarResources;
	}

	public void addKjarResource(KjarResource kjarResource) {
		kjarResources.add(kjarResource);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(kieContainerResource).append(kjarResources).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(kieContainerResource).append(kjarResources).toHashCode();
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
		KieContainerKjarResourcesRequest rhs = (KieContainerKjarResourcesRequest) obj;
		return new EqualsBuilder().append(kieContainerResource, rhs.kieContainerResource)
				.append(kjarResources, rhs.kjarResources).isEquals();
	}

}
