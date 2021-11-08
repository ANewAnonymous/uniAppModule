package com.zc.uniappmodule.update;

public interface MyView {
    void onDownload(int progress);
    void onDownloadFinish(String path);
    void onDownloadError(Exception e);

}
