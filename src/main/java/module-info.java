module com.tugalsan.api.file.img {
    requires gwt.user;
    requires java.desktop;
    requires net.coobird.thumbnailator;
    requires com.tugalsan.api.runnable;
    requires com.tugalsan.api.union;
    requires com.tugalsan.api.thread;
    requires com.tugalsan.api.stream;
    requires com.tugalsan.api.crypto;
    requires com.tugalsan.api.list;
    requires com.tugalsan.api.string;
    requires com.tugalsan.api.shape;
    requires com.tugalsan.api.log;
    requires com.tugalsan.api.random;
    requires com.tugalsan.api.url;
    requires com.tugalsan.api.gui;
    exports com.tugalsan.api.file.img.client;
    exports com.tugalsan.api.file.img.server;
}
