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
@XStreamConverter(value = ToAttributedValueConverter.class, strings = { "scoreString" })
public class ScoreWrapper {

    @XmlAttribute(name = "scoreClass")
    @XStreamAlias(value = "scoreClass")
    private Class<? extends Score> scoreClass;

    @XmlValue
    private String scoreString;

    // Define default constructor to enable class marshalling/unmarshalling
    private ScoreWrapper() {
    }

    public ScoreWrapper( Score score ) {
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
     *
     * @return Score representation of the object. Returns null if the score has not been assigned by the solver yet.
     * @throws IllegalArgumentException If <code>scoreClass</code> is not one of the out-of-box score implementations. In this case
     *                                       clients may implement their own way to extract the score object.
     */
    public Score toScore() {
        if ( scoreClass == null ) {
            return null;
        }

        return ScoreUtils.parseScore( scoreClass, scoreString );
    }

    @Override
    public String toString() {
        return "ScoreWrapper{" +
                "scoreClass='" + scoreClass + '\'' +
                ", scoreString='" + scoreString + '\'' +
                '}';
    }

}
