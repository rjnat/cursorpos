import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useDispatch, useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import { addItem, clearCart, selectCartItems, applyDiscount } from '../../store/cartSlice';
import ProductSearch from '../../components/ProductSearch';
import ProductGrid from '../../components/ProductGrid';
import ProductGridSkeleton from '../../components/ProductGridSkeleton';
import EmptyState from '../../components/EmptyState';
import Cart from '../../components/Cart';
import CartSummary from '../../components/CartSummary';
import DiscountManager from '../../components/DiscountManager';
import CheckoutModal from '../../components/CheckoutModal';
import ReceiptModal from '../../components/ReceiptModal';
import ManagerApprovalModal from '../../components/ManagerApprovalModal';
import { searchProducts, getAllProducts } from '../../services/productService';
import { createTransaction } from '../../services/transactionService';

function Sell() {
  const { t } = useTranslation();
  const dispatch = useDispatch();
  const cartItems = useSelector(selectCartItems);
  const subtotal = useSelector(state => state.cart.subtotal);

  // Product state
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  // Modal state
  const [isCheckoutOpen, setIsCheckoutOpen] = useState(false);
  const [isReceiptOpen, setIsReceiptOpen] = useState(false);
  const [completedTransaction, setCompletedTransaction] = useState(null);
  
  // Manager approval state
  const [isApprovalOpen, setIsApprovalOpen] = useState(false);
  const [approvalRequest, setApprovalRequest] = useState(null);

  // Get user info from localStorage
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const storeId = user.storeId;

  // Load initial products
  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getAllProducts(0, 50);
      setProducts(response.content || []);
    } catch (err) {
      console.error('Error loading products:', err);
      setError(err.response?.data?.message || t('pos.errorLoadingProducts'));
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (query) => {
    setSearchQuery(query);
    setLoading(true);
    setError(null);
    try {
      const response = await searchProducts(query, storeId, 0, 50);
      setProducts(response.content || []);
    } catch (err) {
      console.error('Error searching products:', err);
      setError(err.response?.data?.message || t('pos.errorLoadingProducts'));
    } finally {
      setLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchQuery('');
    loadProducts();
  };

  const handleAddToCart = (product) => {
    dispatch(addItem(product));
    const message = t('pos.addedToCart', { name: product.name });
    toast.success(message, { duration: 2000 });
  };

  const handleCheckout = () => {
    if (cartItems.length === 0) return;
    setIsCheckoutOpen(true);
  };

  const handleCompletePayment = async (paymentData) => {
    try {
      // Prepare transaction data
      const transactionData = {
        branchId: user.branchId,
        type: 'SALE',
        items: cartItems.map(item => ({
          productId: item.id,
          productCode: item.sku,
          productName: item.name,
          quantity: item.quantity,
          unitPrice: item.basePrice,
          taxRate: item.taxRate || 0,
        })),
        payments: [{
          paymentMethod: paymentData.paymentMethod,
          amount: paymentData.paidAmount,
        }],
        discountAmount: 0, // TODO: Implement discount logic
      };

      // Create transaction
      const transaction = await createTransaction(transactionData);
      
      // Show receipt
      setCompletedTransaction(transaction);
      setIsCheckoutOpen(false);
      setIsReceiptOpen(true);
    } catch (err) {
      console.error('Error creating transaction:', err);
      throw err;
    }
  };

  const handleNewOrder = () => {
    dispatch(clearCart());
    setCompletedTransaction(null);
    loadProducts();
  };

  // Manager approval handlers
  const handleRequestApproval = (request) => {
    setApprovalRequest(request);
    setIsApprovalOpen(true);
  };

  const handleApprovalGranted = (managerInfo) => {
    if (approvalRequest && approvalRequest.type === 'DISCOUNT') {
      // Apply the approved discount
      dispatch(applyDiscount({
        type: approvalRequest.discountType,
        value: approvalRequest.discountValue,
        amount: approvalRequest.discountAmount,
        approvedBy: managerInfo.managerName,
        approvedAt: managerInfo.approvedAt
      }));
      
      toast.success(t('pos.approvalGranted'));
    }
    
    setIsApprovalOpen(false);
    setApprovalRequest(null);
  };

  const handleApprovalDenied = () => {
    toast.error(t('pos.approvalDenied'));
    setIsApprovalOpen(false);
    setApprovalRequest(null);
  };

  return (
    <div className="h-full flex flex-col md:flex-row gap-4 p-4 bg-gray-50">
      {/* Left Panel - Products */}
      <div className="flex-1 flex flex-col space-y-4 min-w-0">
        {/* Search */}
        <div className="bg-white rounded-lg shadow p-4">
          <ProductSearch 
            onSearch={handleSearch}
            onClear={handleClearSearch}
          />
        </div>

        {/* Product Grid */}
        <div className="flex-1 bg-white rounded-lg shadow p-4 overflow-y-auto">
          {loading ? (
            <ProductGridSkeleton count={12} />
          ) : error ? (
            <EmptyState
              icon="⚠️"
              title={t('pos.errorLoadingProducts', 'Error Loading Products')}
              message={error}
              action={loadProducts}
              actionLabel={t('common.retry', 'Retry')}
            />
          ) : products.length === 0 ? (
            <EmptyState
              icon="🔍"
              title={searchQuery ? t('pos.noSearchResults', 'No products found') : t('pos.noProducts', 'No products available')}
              message={searchQuery 
                ? t('pos.tryDifferentSearch', 'Try a different search term')
                : t('pos.addProductsFirst', 'Add products to get started')}
              action={searchQuery ? handleClearSearch : undefined}
              actionLabel={searchQuery ? t('pos.clearSearch', 'Clear Search') : undefined}
            />
          ) : (
            <ProductGrid
              products={products}
              onAddToCart={handleAddToCart}
              loading={loading}
              error={error}
            />
          )}
        </div>
      </div>

      {/* Right Panel - Cart */}
      <div className="w-full md:w-96 flex flex-col space-y-4">
        {/* Cart Items */}
        <div className="flex-1 bg-white rounded-lg shadow p-4 overflow-y-auto">
          <h2 className="text-xl font-bold text-gray-900 mb-4">
            {t('pos.cart')}
          </h2>
          <Cart items={cartItems} />
        </div>

        {/* Discount Manager */}
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={handleRequestApproval}
        />

        {/* Cart Summary */}
        <CartSummary onCheckout={handleCheckout} />
      </div>

      {/* Modals */}
      <CheckoutModal
        isOpen={isCheckoutOpen}
        onClose={() => setIsCheckoutOpen(false)}
        onComplete={handleCompletePayment}
      />

      <ReceiptModal
        isOpen={isReceiptOpen}
        onClose={() => setIsReceiptOpen(false)}
        transaction={completedTransaction}
        onNewOrder={handleNewOrder}
      />

      <ManagerApprovalModal
        isOpen={isApprovalOpen}
        onClose={handleApprovalDenied}
        onApprove={handleApprovalGranted}
        request={approvalRequest}
      />
    </div>
  );
}

export default Sell;
