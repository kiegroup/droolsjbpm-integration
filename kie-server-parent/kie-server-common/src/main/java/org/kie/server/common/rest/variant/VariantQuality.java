/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.common.rest.variant;

import java.math.BigDecimal;
import java.math.MathContext;


/**
 * Copied from RestEasy 2.3.6.Final with no modifications
 * Can be deleted when RESTEASY-960 is fixed
 * 
 * A individual variant quality bean for the <span title="Remote Variant Selection Algorithm">RVSA</span>.
 *
 * @see "RFC 2296"
 */
public class VariantQuality
{

   private QualityValue mediaTypeQualityValue = QualityValue.DEFAULT;
   private QualityValue characterSetQualityValue = QualityValue.DEFAULT;
   private QualityValue encodingQualityValue = QualityValue.DEFAULT;
   private QualityValue languageQualityValue = QualityValue.DEFAULT;


   public VariantQuality()
   {
   }


   public void setMediaTypeQualityValue(QualityValue value)
   {
      if (value == null)
         mediaTypeQualityValue = QualityValue.DEFAULT;
      else
         mediaTypeQualityValue = value;
   }


   public void setCharacterSetQualityValue(QualityValue value)
   {
      if (value == null)
         characterSetQualityValue = QualityValue.DEFAULT;
      else
         characterSetQualityValue = value;
   }


   public void setEncodingQualityValue(QualityValue value)
   {
      if (value == null)
         encodingQualityValue = QualityValue.DEFAULT;
      else
         encodingQualityValue = value;
   }


   public void setLanguageQualityValue(QualityValue value)
   {
      if (value == null)
         languageQualityValue = QualityValue.DEFAULT;
      else
         languageQualityValue = value;
   }


   /**
    * @return the quality value between zero and one with five decimal places after the point.
    * @see "3.3 Computing overall quality values"
    */
   public BigDecimal getOverallQuality()
   {
      BigDecimal qt = BigDecimal.valueOf(mediaTypeQualityValue.intValue(), 3);
      BigDecimal qc = BigDecimal.valueOf(characterSetQualityValue.intValue(), 3);
      BigDecimal qe = BigDecimal.valueOf(encodingQualityValue.intValue(), 3);
      BigDecimal ql = BigDecimal.valueOf(languageQualityValue.intValue(), 3);
      assert qt.compareTo(BigDecimal.ZERO) >= 0 && qt.compareTo(BigDecimal.ONE) <= 0;
      assert qc.compareTo(BigDecimal.ZERO) >= 0 && qc.compareTo(BigDecimal.ONE) <= 0;
      assert qe.compareTo(BigDecimal.ZERO) >= 0 && qe.compareTo(BigDecimal.ONE) <= 0;
      assert ql.compareTo(BigDecimal.ZERO) >= 0 && ql.compareTo(BigDecimal.ONE) <= 0;

      BigDecimal result = qt;
      result = result.multiply(qc, MathContext.DECIMAL32);
      result = result.multiply(qe, MathContext.DECIMAL32);
      result = result.multiply(ql, MathContext.DECIMAL32);
      assert result.compareTo(BigDecimal.ZERO) >= 0 && result.compareTo(BigDecimal.ONE) <= 0;

      long round5 = result.scaleByPowerOfTen(5).longValue();
      result = BigDecimal.valueOf(round5, 5);
      assert result.compareTo(BigDecimal.ZERO) >= 0 && result.compareTo(BigDecimal.ONE) <= 0;

      return result;
   }

}
