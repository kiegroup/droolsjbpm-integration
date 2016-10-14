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



/**
 * Copied from RestEasy 2.3.6.Final with very minor modifications (RestOperationException)
 * Can be deleted when RESTEASY-960 is fixed
 *  
 * @author Pascal S. de Kloe
 * @see "RFC2616 3.9 Quality Values"
 */
public final class QualityValue extends Number implements Comparable<QualityValue>
{

   public static final QualityValue NOT_ACCEPTABLE = new QualityValue(0);
   public static final QualityValue LOWEST = new QualityValue(1);
   public static final QualityValue HIGHEST = new QualityValue(1000);
   public static final QualityValue DEFAULT = HIGHEST;

   private static final long serialVersionUID = 1L;
   private static final String MALFORMED_VALUE_MESSAGE = "Malformed quality value.";

   private final int WEIGHT;


   private QualityValue(int weight)
   {
      assert weight >= 0;
      assert weight <= 1000;
      WEIGHT = weight;
   }


   /**
    * @param qvalue the quality value or {@code null} if undefined.
    */
   public static QualityValue valueOf(String qvalue)
   {
      if (qvalue == null)
         return DEFAULT;
      return new QualityValue(parseAsInteger(qvalue));
   }


   public boolean isPrefered()
   {
      return WEIGHT == HIGHEST.WEIGHT;
   }


   public boolean isAcceptable()
   {
      return WEIGHT != NOT_ACCEPTABLE.WEIGHT;
   }


   public int compareTo(QualityValue o)
   {
      return WEIGHT - o.WEIGHT;
   }


   @Override
   public boolean equals(Object o)
   {
      if (o == null || o.getClass() != QualityValue.class)
         return false;
      QualityValue other = (QualityValue) o;
      return WEIGHT == other.WEIGHT;
   }


   @Override
   public int hashCode()
   {
      return WEIGHT;
   }


   @Override
   public double doubleValue()
   {
      return (double) WEIGHT / 1000d;
   }


   @Override
   public float floatValue()
   {
      return (float) WEIGHT / 1000f;
   }


   @Override
   public int intValue()
   {
      return WEIGHT;
   }


   @Override
   public long longValue()
   {
      return WEIGHT;
   }


   private static int parseAsInteger(String value)
   {
      int length = value.length();
      if (length == 0 || length > 5)
         throw new IllegalArgumentException(MALFORMED_VALUE_MESSAGE);
      if (length > 1 && value.charAt(1) != '.')
         throw new IllegalArgumentException(MALFORMED_VALUE_MESSAGE);
      int firstCharacter = value.codePointAt(0);
      if (firstCharacter == '1')
      {
         for (int i = 2; i < length; ++i)
            if (value.charAt(i) != '0')
               throw new IllegalArgumentException(MALFORMED_VALUE_MESSAGE);
         return 1000;
      }
      else if (firstCharacter == '0')
      {
         int weight = 0;
         for (int i = 2; i < 5; ++i)
         {
            weight *= 10;
            if (i < length)
            {
               int digit = value.codePointAt(i) - '0';
               if (digit < 0 || digit > 9)
                  throw new IllegalArgumentException(MALFORMED_VALUE_MESSAGE);
               weight += digit;
            }
         }
         return weight;
      }
      else
         throw new IllegalArgumentException(MALFORMED_VALUE_MESSAGE);
   }

}

