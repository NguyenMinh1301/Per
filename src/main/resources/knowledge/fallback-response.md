# Fallback Response (No Products Found)

When the vector store returns no relevant documents OR the inventory is empty, use this response structure:

## English Version
```
I apologize, but I couldn't find specific products matching your request in our current inventory.

However, I'd love to help you find your perfect fragrance! Could you share:
- What fragrance families do you typically enjoy? (Floral, Woody, Fresh, Oriental)
- What's the occasion? (Daily wear, special events, office)
- Any favorite scents or notes you're drawn to?

Our boutique specializes in premium fragrances with a curated selection across all major families. I'm here to guide you to something truly special.
```

## Vietnamese Version
```
Xin lỗi, tôi không tìm thấy sản phẩm cụ thể phù hợp với yêu cầu của bạn trong kho hiện tại.

Tuy nhiên, tôi rất muốn giúp bạn tìm được mùi hương hoàn hảo! Bạn có thể chia sẻ:
- Bạn thường thích các họ hương gì? (Hoa, Gỗ, Tươi mát, Phương đông)
- Dịp sử dụng là gì? (Hàng ngày, sự kiện đặc biệt, văn phòng)
- Có mùi hương hoặc nốt hương nào bạn yêu thích không?

Cửa hàng của chúng tôi chuyên về nước hoa cao cấp với bộ sưu tập được tuyển chọn kỹ lưỡng. Tôi sẵn sàng hướng dẫn bạn đến mùi hương thật sự đặc biệt.
```

## JSON Structure
```json
{
  "summary": "No matching products found. Let me help you discover your ideal fragrance.",
  "detailedResponse": "[Use appropriate language version above]",
  "products": [],
  "nextSteps": [
    "Browse our full catalog",
    "Tell me your favorite scent notes",
    "Explore our best-sellers"
  ]
}
```
