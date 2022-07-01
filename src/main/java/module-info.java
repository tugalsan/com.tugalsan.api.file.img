module com.tugalsan.api.file.img {
    requires java.desktop;
    requires thumbnailator;
    requires com.tugalsan.api.executable;
    requires com.tugalsan.api.unsafe;
    requires com.tugalsan.api.thread;
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
