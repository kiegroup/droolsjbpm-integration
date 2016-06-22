/*
 * Copyright 2016 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(type = BendableScore.class, value = BendableScoreJaxbXmlAdapter.class),
    @XmlJavaTypeAdapter(type = BendableLongScore.class, value = BendableLongScoreJaxbXmlAdapter.class),
    @XmlJavaTypeAdapter(type = HardMediumSoftScore.class, value = HardMediumSoftScoreJaxbXmlAdapter.class),
    @XmlJavaTypeAdapter(type = HardMediumSoftLongScore.class, value = HardMediumSoftLongScoreJaxbXmlAdapter.class),
    @XmlJavaTypeAdapter(type = HardSoftScore.class, value = HardSoftScoreJaxbXmlAdapter.class),
    @XmlJavaTypeAdapter(type = HardSoftLongScore.class, value = HardSoftLongScoreJaxbXmlAdapter.class),
    @XmlJavaTypeAdapter(type = HardSoftDoubleScore.class, value = HardSoftDoubleScoreJaxbXmlAdapter.class),
    @XmlJavaTypeAdapter(type = HardSoftBigDecimalScore.class, value = HardSoftBigDecimalScoreJaxbXmlAdapter.class),
    @XmlJavaTypeAdapter(type = SimpleScore.class, value = SimpleScoreJaxbXmlAdapter.class),
    @XmlJavaTypeAdapter(type = SimpleLongScore.class, value = SimpleLongScoreJaxbXmlAdapter.class),
    @XmlJavaTypeAdapter(type = SimpleDoubleScore.class, value = SimpleDoubleScoreJaxbXmlAdapter.class),
    @XmlJavaTypeAdapter(type = SimpleBigDecimalScore.class, value = SimpleBigDecimalScoreJaxbXmlAdapter.class)
})
package org.kie.server.api;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.api.score.buildin.hardsoftdouble.HardSoftDoubleScore;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;
import org.optaplanner.core.api.score.buildin.simpledouble.SimpleDoubleScore;
import org.optaplanner.core.api.score.buildin.simplelong.SimpleLongScore;
import org.optaplanner.persistence.jaxb.api.score.buildin.bendable.BendableScoreJaxbXmlAdapter;
import org.optaplanner.persistence.jaxb.api.score.buildin.bendablelong.BendableLongScoreJaxbXmlAdapter;
import org.optaplanner.persistence.jaxb.api.score.buildin.hardmediumsoft.HardMediumSoftScoreJaxbXmlAdapter;
import org.optaplanner.persistence.jaxb.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScoreJaxbXmlAdapter;
import org.optaplanner.persistence.jaxb.api.score.buildin.hardsoft.HardSoftScoreJaxbXmlAdapter;
import org.optaplanner.persistence.jaxb.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScoreJaxbXmlAdapter;
import org.optaplanner.persistence.jaxb.api.score.buildin.hardsoftdouble.HardSoftDoubleScoreJaxbXmlAdapter;
import org.optaplanner.persistence.jaxb.api.score.buildin.hardsoftlong.HardSoftLongScoreJaxbXmlAdapter;
import org.optaplanner.persistence.jaxb.api.score.buildin.simple.SimpleScoreJaxbXmlAdapter;
import org.optaplanner.persistence.jaxb.api.score.buildin.simplebigdecimal.SimpleBigDecimalScoreJaxbXmlAdapter;
import org.optaplanner.persistence.jaxb.api.score.buildin.simpledouble.SimpleDoubleScoreJaxbXmlAdapter;
import org.optaplanner.persistence.jaxb.api.score.buildin.simplelong.SimpleLongScoreJaxbXmlAdapter;
