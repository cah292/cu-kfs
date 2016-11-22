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
package edu.cornell.kfs.paymentworks.service;

import java.io.File;

import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;

public interface PaymentWorksKfsService {

	public boolean routeNewVendor(PaymentWorksVendor paymentWorksVendor);

	public boolean routeVendorEdit(PaymentWorksVendor paymentWorksVendor);

	public boolean directVendorEdit(PaymentWorksVendor paymentWorksVendor);

	public boolean directAchEdit(PaymentWorksVendorUpdatesDTO vendorUpdate, String vendorNumberList);

	public void sendEmailVendorInitiated(String documentNumber, String vendorName, String contactEmail);

	public void sendEmailVendorApproved(String documentNumber, String vendorNumber, String vendorName,
			String contactEmail);

	public void sendSummaryEmail(File reportFile, String subject);

}
