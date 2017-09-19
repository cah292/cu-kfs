/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.paymentworks.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "list-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksVendorNumberDTO {

    private String vendor_num;
    private PaymentWorksSiteCodesDTO site_codes;

    public String getVendor_num() {
        return vendor_num;
    }

    public void setVendor_num(String vendor_num) {
        this.vendor_num = vendor_num;
    }

    public PaymentWorksSiteCodesDTO getSite_codes() {
        return site_codes;
    }

    public void setSite_codes(PaymentWorksSiteCodesDTO site_codes) {
        this.site_codes = site_codes;
    }

}
