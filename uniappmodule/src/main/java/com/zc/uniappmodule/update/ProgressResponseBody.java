package com.zc.uniappmodule.update;

import java.io.IOException;

import dc.squareup.okhttp3.MediaType;
import dc.squareup.okhttp3.ResponseBody;
import dc.squareup.okio.Buffer;
import dc.squareup.okio.BufferedSource;
import dc.squareup.okio.ForwardingSource;
import dc.squareup.okio.Okio;
import dc.squareup.okio.Source;

public class ProgressResponseBody extends ResponseBody {

    //回调接口
    public interface ProgressListener{
        /**
         * @param bytesRead 已经读取的字节数
         * @param contentLength 响应总长度
         * @param done 是否读取完毕
         */
        void update(long bytesRead,long contentLength,boolean done);
    }

    private final ResponseBody responseBody;
    private final ProgressListener progressListener;
    private BufferedSource bufferedSource;

    public ProgressResponseBody(ResponseBody responseBody,ProgressListener progressListener){
        this.responseBody = responseBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null){
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }


    private Source source(Source source){
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink,byteCount);
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;   //不断统计当前下载好的数据
                //接口回调
                progressListener.update(totalBytesRead,responseBody.contentLength(),bytesRead == -1);
                return bytesRead;
            }
        };
    }
}
