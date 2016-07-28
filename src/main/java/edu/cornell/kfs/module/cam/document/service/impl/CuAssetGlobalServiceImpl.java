package edu.cornell.kfs.module.cam.document.service.impl;

import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.businessobject.AssetGlpeSourceDetail;
import org.kuali.kfs.module.cam.businessobject.AssetPaymentDetail;
import org.kuali.kfs.module.cam.document.service.impl.AssetGlobalServiceImpl;

import edu.cornell.kfs.module.cam.businessobject.AssetExtension;
import edu.cornell.kfs.module.cam.document.service.CuAssetSubAccountService;

public class CuAssetGlobalServiceImpl extends AssetGlobalServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAssetGlobalServiceImpl.class);

    protected CuAssetSubAccountService cuAssetSubAccountService;

    // KFSUPGRADE-535
    // if we need to implement service rate ind, then it can be populated from detail to assetext too.
    @Override
    protected Asset setupAsset(AssetGlobal assetGlobal, AssetGlobalDetail assetGlobalDetail, boolean separate) {
        Asset asset =  super.setupAsset(assetGlobal, assetGlobalDetail, separate);
        AssetExtension ae = (AssetExtension) asset.getExtension();
        ae.setCapitalAssetNumber(asset.getCapitalAssetNumber());
        return asset;
    }

    /**
     * Overridden to forcibly set the sub-account number on the generated postable object to null, if the account number
     * is flagged as being one that should not have a sub-account number defined on related source details.
     * The "ASSET_PLANT_ACCOUNTS_TO_FORCE_CLEARING_OF_GLPE_SUB_ACCOUNTS" parameter governs whether such clearing behavior should be applied;
     * see shouldPreserveSubAccount() for details.
     *
     * @see org.kuali.kfs.module.cam.document.service.impl.AssetGlobalServiceImpl#createAssetGlpePostable(
     * org.kuali.kfs.module.cam.businessobject.AssetGlobal, org.kuali.kfs.module.cam.businessobject.AssetPaymentDetail,
     * org.kuali.kfs.module.cam.document.service.impl.AssetGlobalServiceImpl.AmountCategory)
     */
    @Override
    protected AssetGlpeSourceDetail createAssetGlpePostable(AssetGlobal document, AssetPaymentDetail assetPaymentDetail, AmountCategory amountCategory) {
        AssetGlpeSourceDetail postable = super.createAssetGlpePostable(document, assetPaymentDetail, amountCategory);
        getCuAssetSubAccountService().clearSubAccountIfNecessary(postable);
        return postable;
    }

    public void setCuAssetSubAccountService(CuAssetSubAccountService cuAssetSubAccountService) {
        this.cuAssetSubAccountService = cuAssetSubAccountService;
    }

    public CuAssetSubAccountService getCuAssetSubAccountService() {
        return this.cuAssetSubAccountService;
    }

}
