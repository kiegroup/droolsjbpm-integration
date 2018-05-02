package com.acme.functions;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.math.BigDecimal;
import java.util.List;

public class StandardDeviation {

    public static BigDecimal std( List<Number> values ) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for( Number value : values ) {
            stats.addValue( value.doubleValue() );
        }
        return new BigDecimal( stats.getStandardDeviation() );
    }
}
