package edu.cornell.kfs.tax.batch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Helper object defining a specific section of the tax file output.
 * 
 * <p>The "length" property indicates either the max length or the exact length of
 * this section's text output, depending on the output format. (It must be nonnegative.)
 * Note that this length includes that of any expected separator characters and any
 * other sections that have been appended to this one, so it is not necessarily
 * the same as the total length of the section's given fields.</p>
 * 
 * <p>The "fields" property contains the definitions of the various pieces/fields
 * that will be included in the output.</p>
 */
public class TaxOutputSection {
    // Either the max length or exact length of this section's expected text output.
    private Integer length;
    // Indicates whether the section's and its fields' max lengths should be treated as exact lengths.
    private boolean hasExactLength;
    // Indicates whether to append a separator character between each field in the output.
    private boolean hasSeparators;
    // The separator character to use, if hasSeparator is set to true.
    private Character separatorChar;
    // The tax fields for this section's output.
    private List<TaxOutputField> fields;

    public TaxOutputSection() {
        fields = new ArrayList<TaxOutputField>();
    }



    /**
     * Returns the max length or exact length for each text line generated by this section.
     */
    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * Returns whether the length property represents an exact length (true) or just a max length (false);
     * default is false.
     */
    public boolean isHasExactLength() {
        return hasExactLength;
    }

    public void setHasExactLength(boolean hasExactLength) {
        this.hasExactLength = hasExactLength;
    }

    /**
     * Returns whether to place a separator character in between each field's output; default is false.
     */
    public boolean isHasSeparators() {
        return hasSeparators;
    }

    public void setHasSeparators(boolean hasSeparators) {
        this.hasSeparators = hasSeparators;
    }

    /**
     * Returns the separator (as a Character object) to place in between each field's output;
     * ignored if hasSeparators is false.
     */
    public Character getSeparatorChar() {
        return separatorChar;
    }

    public void setSeparatorChar(Character separatorChar) {
        this.separatorChar = separatorChar;
    }

    /**
     * Returns the separator (as a single-character String object) to place in between each field's output;
     * ignored if hasSeparators is false.
     */
    public String getSeparator() {
        return (separatorChar != null) ? separatorChar.toString() : null;
    }

    public void setSeparator(String separator) {
        this.separatorChar = (StringUtils.isNotEmpty(separator)) ? Character.valueOf(StringEscapeUtils.unescapeJava(separator).charAt(0)) : null;
    }

    /**
     * Returns the output field definitions to use for generating the row contents for the tax file.
     */
    public List<TaxOutputField> getFields() {
        return fields;
    }

    public void setFields(List<TaxOutputField> fields) {
        this.fields = fields;
    }

    public void addField(TaxOutputField field) {
        fields.add(field);
    }
}
