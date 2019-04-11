/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.model.instance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.ScoreUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"scoreString"})
public class ScoreWrapper {

    @XmlAttribute(name = "scoreClass")
    @XStreamAlias(value = "scoreClass")
    private Class<? extends Score> scoreClass;

    @XmlValue
    private String scoreString;

    // Define default constructor to enable class marshalling/unmarshalling
    private ScoreWrapper() {
    }

    public ScoreWrapper(Score score) {
        this.scoreClass = score == null ? null : score.getClass();
        this.scoreString = score == null ? null : score.toString();
    }

    public Class<? extends Score> getScoreClass() {
        return scoreClass;
    }

    public String getScoreString() {
        return scoreString;
    }

    /**
     * Returns score representation of the object.
     * @return Score representation of the object. Returns null if the score has not been assigned by the solver yet.
     * @throws IllegalArgumentException If <code>scoreClass</code> is not one of the out-of-box score implementations.
     * In this case clients may implement their own way to extract the score object.
     */
    public Score toScore() {
        if (scoreClass == null) {
            return null;
        }

        return ScoreUtils.parseScore(scoreClass, scoreString);
    }

    @Override
    public String toString() {
        return "ScoreWrapper{"
                + "scoreClass='" + scoreClass + '\''
                + ", scoreString='" + scoreString + '\''
                + '}';
    }
}
