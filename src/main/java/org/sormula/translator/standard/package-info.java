/**
 * Classes that convert values to/from standard Java data types and to/from JDBC
 * parameters. Use these translators in {@link org.sormula.annotation.Column#translator()}
 * to override default translantor or to define custom translators.
 * TODO
 * note that {@link org.sormula.translator.ColumnTranslator} is deprecated. Use
 * ... instead
 */
package org.sormula.translator.standard;