/**
 * ProductCardSkeleton Component
 * Loading skeleton for product cards
 */
const ProductCardSkeleton = () => {
  return (
    <div className="bg-white rounded-lg shadow-sm border-2 border-transparent overflow-hidden animate-pulse">
      {/* Image skeleton */}
      <div className="h-32 bg-gray-200" />
      
      {/* Content skeleton */}
      <div className="p-4">
        {/* Title */}
        <div className="mb-2">
          <div className="h-4 bg-gray-200 rounded w-3/4 mb-2" />
          <div className="h-3 bg-gray-200 rounded w-1/2" />
        </div>
        
        {/* Price */}
        <div className="h-6 bg-gray-200 rounded w-1/3 mb-2" />
        
        {/* Stock */}
        <div className="h-3 bg-gray-200 rounded w-1/4 mb-3" />
        
        {/* Button */}
        <div className="h-10 bg-gray-200 rounded" />
      </div>
    </div>
  );
};

/**
 * ProductGridSkeleton Component
 * Shows multiple product card skeletons
 */
const ProductGridSkeleton = ({ count = 12 }) => {
  return (
    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
      {Array.from({ length: count }).map((_, index) => (
        <ProductCardSkeleton key={index} />
      ))}
    </div>
  );
};

export { ProductCardSkeleton, ProductGridSkeleton };
export default ProductGridSkeleton;
