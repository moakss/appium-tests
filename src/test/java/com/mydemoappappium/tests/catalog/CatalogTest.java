package com.mydemoappappium.tests.catalog;

import com.mydemoappappium.pages.CatalogPage;
import com.mydemoappappium.pages.SortOrder;
import com.mydemoappappium.testdata.Product;
import com.mydemoappappium.testdata.TestData;
import io.qameta.allure.TmsLink;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CatalogTest {

    @TmsLink("TC-01")
    @Test(groups = {"smoke"}, description = "TC-01 App launches to Catalog")
    public void shouldOpenCatalogOnLaunch() {
        Product backpack = TestData.product("backpack");

        CatalogPage catalog = CatalogPage.atLaunch();

        assertThat(catalog.visibleProductNames()).as("visible product names").contains(backpack.name());
        assertThat(catalog.cartItemCount()).as("cart badge on fresh launch").isZero();
    }

    @DataProvider
    public Object[][] sortOrders() {
        return new Object[][]{
                {SortOrder.NAME_ASCENDING}, {SortOrder.NAME_DESCENDING},
                {SortOrder.PRICE_ASCENDING}, {SortOrder.PRICE_DESCENDING}};
    }

    @TmsLink("TC-12")
    @Test(groups = {"regression"}, dataProvider = "sortOrders", description = "TC-12 Catalog sorting")
    public void shouldSortCatalog(SortOrder order) {
        List<String> names = CatalogPage.atLaunch().sortBy(order).visibleProductNames();
        assertThat(names).as("visible products after sorting").isNotEmpty();

        // iOS does not expose catalog prices, so price order is checked via the product family prices from test data.
        List<BigDecimal> prices = names.stream().map(TestData::familyPrice).toList();
        switch (order) {
            case NAME_ASCENDING -> assertThat(names).as("names ascending").isSortedAccordingTo(Comparator.naturalOrder());
            case NAME_DESCENDING -> assertThat(names).as("names descending").isSortedAccordingTo(Comparator.reverseOrder());
            case PRICE_ASCENDING -> assertThat(prices).as("prices ascending of " + names)
                    .isSortedAccordingTo(Comparator.naturalOrder());
            case PRICE_DESCENDING -> assertThat(prices).as("prices descending of " + names)
                    .isSortedAccordingTo(Comparator.reverseOrder());
        }
    }
}
