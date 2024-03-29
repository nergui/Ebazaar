package business.externalinterfaces;

import java.util.List;


public interface ShoppingCart {
    Address getShippingAddress();
    Address getBillingAddress();
    CreditCard getPaymentInfo();
    List<CartItem> getCartItems();
    void setCartItems(List<CartItem> cartItems);
    void clearCart();
    double getTotalPrice();
    boolean deleteCartItem(String name);
    boolean isEmpty();

}
