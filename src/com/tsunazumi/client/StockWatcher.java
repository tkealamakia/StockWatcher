package com.tsunazumi.client;

import com.google.gwt.core.client.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class StockWatcher implements EntryPoint {
    Logger logger = Logger.getLogger("StockWatcher");
    private static final int REFRESH_INTERVAL = 5000;
    private VerticalPanel mainPanel = new VerticalPanel();
    private FlexTable stocksFlexTable = new FlexTable();
    private HorizontalPanel addPanel = new HorizontalPanel();
    private TextBox newSymbolTextBox = new TextBox();
    private Button addStockButton = new Button("Add");
    private Label lastUpdatedLabel = new Label();
    private ArrayList<String> stocks = new ArrayList<>();
    private StockPriceServiceAsync stockPriceSvc = GWT.create(StockPriceService.class);
    private static final String JSON_URL = "http://127.0.0.1:8090/stockPrices?q=";

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        stocksFlexTable.setText(0, 0, "Symbol");
        stocksFlexTable.setText(0, 1, "Price");
        stocksFlexTable.setText(0, 2, "Change");
        stocksFlexTable.setText(0, 3, "Remove");

        addPanel.add(newSymbolTextBox);
        addPanel.add(addStockButton);

        mainPanel.add(stocksFlexTable);
        mainPanel.add(addPanel);
        mainPanel.add(lastUpdatedLabel);

        RootPanel.get("stockList").add(mainPanel);

        newSymbolTextBox.setFocus(true);

        Timer refreshTimer = new Timer() {
            @Override
            public void run() {
                refreshWatchList();
            }
        };
        refreshTimer.scheduleRepeating(REFRESH_INTERVAL);

        addStockButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addStock();
            }
        });

        newSymbolTextBox.addKeyDownHandler(event -> {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                addStock();
            }
        });

    }

    private void addStock() {
        final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
        newSymbolTextBox.setFocus(true);

        // Stock code must be between 1 and 10 chars that are numbers, letters, or dots.
        if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
            Window.alert("'" + symbol + "' is not a valid symbol.");
            newSymbolTextBox.selectAll();
            return;
        }

        newSymbolTextBox.setText("");

        if (stocks.contains(symbol)) {
            return;
        }
        int row = stocksFlexTable.getRowCount();
        stocks.add(symbol);
        stocksFlexTable.setText(row, 0, symbol);

        Button removeStockButton = new Button("x");
        removeStockButton.addClickHandler(event -> {
            int removedIndex = stocks.indexOf(symbol);
            stocks.remove(removedIndex);
            stocksFlexTable.removeRow(removedIndex + 1);
        });
        stocksFlexTable.setWidget(row, 3, removeStockButton);

        refreshWatchList();

    }

    private void refreshWatchList() {
        logger.log(Level.SEVERE, "testing this out 1");
        if (stocks.size() == 0) {
            return;
        }

        String url = JSON_URL;

        Iterator<String> iter = stocks.iterator();
        while (iter.hasNext()) {
            url += iter.next();
            if (iter.hasNext()) {
                url += "+";
            }
        }
        url = URL.encode(url);
        logger.log(Level.SEVERE, url);

        JsonpRequestBuilder builder = new JsonpRequestBuilder();
        builder.setCallbackParam("getStock");
        logger.log(Level.SEVERE, "check2");
        builder.requestObject(url, new AsyncCallback<JsArray<StockData>>() {
            @Override
            public void onFailure(Throwable caught) {

                logger.log(Level.SEVERE, "foobar!");
                logger.log(Level.SEVERE, caught.getMessage());
            }

            @Override
            public void onSuccess(JsArray<StockData> stockData) {
                if (stockData == null) {
                    logger.log(Level.SEVERE, "stockData is null");
                    return;
                }
                logger.log(Level.SEVERE, stockData.toString());
                updateTable(stockData);
            }

        });

    }

    private void updateTable(JsArray<StockData> prices) {
        logger.log(Level.SEVERE, prices.get(0).getSymbol());
        for (int i = 0; i < prices.length(); i++) {
            logger.log(Level.SEVERE, prices.get(i).getSymbol());
            updateTable(prices.get(i));
        }
        // Display timestamp showing last refresh.
        DateTimeFormat dateFormat = DateTimeFormat.getFormat(
                DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
        lastUpdatedLabel.setText("Last update : "
                + dateFormat.format(new Date()));
    }

    private void updateTable(StockData price) {     // Make sure the stock is still in the stock table.
        logger.log(Level.SEVERE, "in here 2");
        if (!stocks.contains(price.getSymbol())) {
            logger.log(Level.SEVERE, "in here 3");
            return;
        }

        int row = stocks.indexOf(price.getSymbol()) + 1;

        // Format the data in the Price and Change fields.
        String priceText = NumberFormat.getFormat("#,##0.00").format(
                price.getPrice());
        NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
        String changeText = changeFormat.format(price.getChange());
        String changePercentText = changeFormat.format(price.getChangePercent());

        // Populate the Price and Change fields with new data.
        stocksFlexTable.setText(row, 1, priceText);
        stocksFlexTable.setText(row, 2, changeText + " (" + changePercentText
                + "%)");
    }

}
