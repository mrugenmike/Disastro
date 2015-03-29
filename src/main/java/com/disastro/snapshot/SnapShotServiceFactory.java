package com.disastro.snapshot;


public class SnapShotServiceFactory {
    public static synchronized SnapShotService instance(){
        return new SnapShotService();
    }
}
