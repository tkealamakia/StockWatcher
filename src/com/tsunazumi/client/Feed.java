package com.tsunazumi.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

class Feed extends JavaScriptObject {                              // (1)
    // Overlay types always have protected, zero argument constructors.
    protected Feed() {}                                              // (2)

    public final native JsArray<Entry> getEntries() /*-{ return this.feed.entry; }-*/;
}

