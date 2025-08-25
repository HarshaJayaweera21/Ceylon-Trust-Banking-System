package com.ceylonbank.webbasedbankingsystem.observer;

import com.ceylonbank.webbasedbankingsystem.entity.BankNews;

public interface NewsObserver {
    void onNewsPosted(BankNews news);
    String getObserverType(); // Returns "CUSTOMER" or "STAFF"
}
