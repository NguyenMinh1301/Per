/*
    Author: nguyenminh1301
    Date:   20/12/2025
    Extended Perfume Demo Data - Part 1: Brands, Categories, Made_id
*/

-- =============================================
-- BRANDS (20 brands - luxury perfume houses)
-- =============================================
INSERT INTO public.brand (id, name, description, website_url, founded_year, image_public_id, image_url, is_active, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Chanel', 'French luxury fashion house renowned for timeless elegance and iconic fragrances', 'https://www.chanel.com', 1910, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440002', 'Dior', 'Prestigious French luxury brand offering sophisticated haute couture and perfumery', 'https://www.dior.com', 1946, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440003', 'Tom Ford', 'American luxury brand known for bold, sensual fragrances and modern elegance', 'https://www.tomford.com', 2005, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440004', 'Creed', 'Historic luxury fragrance house crafting artisanal perfumes since 1760', 'https://www.creedboutique.com', 1760, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440005', 'Yves Saint Laurent', 'Iconic French fashion house celebrated for revolutionary style and captivating scents', 'https://www.ysl.com', 1961, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440006', 'Hermès', 'French luxury manufacturer renowned for exquisite leather goods and refined fragrances', 'https://www.hermes.com', 1837, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440007', 'Guerlain', 'Legendary French perfume house with heritage dating back to 1828', 'https://www.guerlain.com', 1828, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440008', 'Givenchy', 'French luxury fashion and perfume house embodying Parisian elegance', 'https://www.givenchy.com', 1952, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440009', 'Versace', 'Italian luxury brand famous for bold designs and glamorous fragrances', 'https://www.versace.com', 1978, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440010', 'Prada', 'Italian fashion powerhouse offering innovative luxury goods and sophisticated scents', 'https://www.prada.com', 1913, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440011', 'Burberry', 'British heritage brand distinguished by timeless style and classic fragrances', 'https://www.burberry.com', 1856, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440012', 'Jo Malone London', 'British fragrance brand celebrated for elegant simplicity and cologne artistry', 'https://www.jomalone.com', 1994, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440013', 'Maison Francis Kurkdjian', 'Parisian haute parfumerie creating contemporary luxury fragrances', 'https://www.franciskurkdjian.com', 2009, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440014', 'Byredo', 'Swedish niche perfume brand known for minimalist aesthetics and evocative scents', 'https://www.byredo.com', 2006, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440015', 'Le Labo', 'Artisanal perfume house crafting handmade fragrances with raw materials focus', 'https://www.lelabofragrances.com', 2006, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440016', 'Dolce & Gabbana', 'Italian luxury fashion house known for Mediterranean-inspired fragrances', 'https://www.dolcegabbana.com', 1985, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440017', 'Giorgio Armani', 'Italian fashion empire celebrated for sophisticated and refined perfumes', 'https://www.armanibeauty.com', 1975, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440018', 'Carolina Herrera', 'Venezuelan-American fashion house known for elegant feminine fragrances', 'https://www.carolinaherrera.com', 1981, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440019', 'Acqua di Parma', 'Italian luxury brand famous for classic Mediterranean citrus colognes', 'https://www.acquadiparma.com', 1916, NULL, NULL, true, now(), now()),
('550e8400-e29b-41d4-a716-446655440020', 'Parfums de Marly', 'French niche house inspired by 18th century perfumery traditions', 'https://www.parfums-de-marly.com', 2009, NULL, NULL, true, now(), now());

-- =============================================
-- CATEGORIES (5 fragrance concentration types)
-- =============================================
INSERT INTO public.category (id, name, description, descriptions, image_public_id, image_url, is_active, created_at, updated_at) VALUES
('6ba7b810-9dad-11d1-80b4-00c04fd430c1', 'Eau de Parfum', 'Concentrated fragrance with 15-20% perfume oil offering long-lasting scent', 'Premium concentration ideal for special occasions and all-day wear', NULL, NULL, true, now(), now()),
('6ba7b810-9dad-11d1-80b4-00c04fd430c2', 'Eau de Toilette', 'Light fragrance concentration with 5-15% perfume oil for daily wear', 'Perfect balance of longevity and freshness for everyday use', NULL, NULL, true, now(), now()),
('6ba7b810-9dad-11d1-80b4-00c04fd430c3', 'Parfum', 'Highest concentration of fragrance oils delivering maximum longevity', 'Purest form offering 20-30% oil concentration for the ultimate experience', NULL, NULL, true, now(), now()),
('6ba7b810-9dad-11d1-80b4-00c04fd430c4', 'Cologne', 'Fresh light fragrance with 2-5% perfume oil ideal for casual occasions', 'Traditional refreshing formulation perfect for warm weather', NULL, NULL, true, now(), now()),
('6ba7b810-9dad-11d1-80b4-00c04fd430c5', 'Body Mist', 'Subtle scented spray with minimal oil concentration for refreshing touch', 'Light hydrating formula for a gentle fragrance throughout the day', NULL, NULL, true, now(), now());

-- =============================================
-- MADE_ID (Countries of origin)
-- =============================================
INSERT INTO public.made_id (id, name, iso_code, region, description, image_public_id, image_url, is_active, created_at, updated_at) VALUES
('7c9e6679-7425-40de-944b-e07fc1f90ae1', 'France', 'FR', 'Europe', 'Renowned global center of perfumery excellence and luxury fragrance heritage', NULL, NULL, true, now(), now()),
('7c9e6679-7425-40de-944b-e07fc1f90ae2', 'Italy', 'IT', 'Europe', 'Distinguished for sophisticated craftsmanship and elegant fragrance tradition', NULL, NULL, true, now(), now()),
('7c9e6679-7425-40de-944b-e07fc1f90ae3', 'United Kingdom', 'GB', 'Europe', 'Historic perfume market known for classic cologne and traditional scents', NULL, NULL, true, now(), now()),
('7c9e6679-7425-40de-944b-e07fc1f90ae4', 'United States', 'US', 'North America', 'Modern fragrance innovation hub with diverse contemporary perfume culture', NULL, NULL, true, now(), now()),
('7c9e6679-7425-40de-944b-e07fc1f90ae5', 'Switzerland', 'CH', 'Europe', 'Premium quality perfume production with precision craftsmanship standards', NULL, NULL, true, now(), now()),
('7c9e6679-7425-40de-944b-e07fc1f90ae6', 'Sweden', 'SE', 'Europe', 'Contemporary niche fragrance innovation with minimalist Scandinavian design', NULL, NULL, true, now(), now()),
('7c9e6679-7425-40de-944b-e07fc1f90ae7', 'Spain', 'ES', 'Europe', 'Rich perfumery tradition blending Mediterranean and Arabic influences', NULL, NULL, true, now(), now()),
('7c9e6679-7425-40de-944b-e07fc1f90ae8', 'Germany', 'DE', 'Europe', 'Known for precision formulation and classic eau de cologne traditions', NULL, NULL, true, now(), now());

-- =============================================
-- PRODUCTS (50 perfumes - real luxury fragrances)
-- =============================================
INSERT INTO public.product (id, brand_id, category_id, made_in_id, name, short_description, description, launch_year, image_public_id, image_url, fragrance_family, gender, sillage, longevity, seasonality, occasion, is_limited_edition, is_discontinued, is_active, created_at, updated_at) VALUES
-- Chanel
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '550e8400-e29b-41d4-a716-446655440001', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Chanel No. 5', 'Timeless floral aldehyde masterpiece embodying ultimate femininity', 'Iconic fragrance featuring aldehydes, ylang-ylang, neroli, jasmine, and sandalwood creating legendary sophisticated composition', 1921, NULL, NULL, 'FLORAL_ALDEHYDE', 'FEMALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'EVENING_FORMAL', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', '550e8400-e29b-41d4-a716-446655440001', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Coco Mademoiselle', 'Fresh oriental expressing youthful independence and elegance', 'Modern feminine composition featuring orange, rose, patchouli, and vanilla for confident charm', 2001, NULL, NULL, 'FRESH_ORIENTAL', 'FEMALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'CASUAL_EVENING', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', '550e8400-e29b-41d4-a716-446655440001', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Bleu de Chanel', 'Sophisticated woody aromatic with timeless masculinity', 'Refined blend of citrus, mint, pink pepper, cedar, and sandalwood for elegant confidence', 2010, NULL, NULL, 'WOODY_AROMATIC', 'MALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'BUSINESS_EVENING', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', '550e8400-e29b-41d4-a716-446655440001', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Chance Eau Tendre', 'Delicate floral fruity with romantic softness', 'Tender composition of grapefruit, quince, jasmine, and white musk for feminine grace', 2010, NULL, NULL, 'FLORAL_FRUITY', 'FEMALE', 'MODERATE', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
-- Dior
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', '550e8400-e29b-41d4-a716-446655440002', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Sauvage', 'Fresh spicy fragrance with raw intensity and magnetic appeal', 'Contemporary masculine scent blending Calabrian bergamot, Sichuan pepper, and ambroxan for powerful signature', 2015, NULL, NULL, 'AROMATIC_FOUGERE', 'MALE', 'STRONG', 'VERY_LONG_LASTING', 'ALL_SEASONS', 'CASUAL_EVENING', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', '550e8400-e29b-41d4-a716-446655440002', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Miss Dior', 'Elegant floral chypre celebrating couture femininity', 'Romantic bouquet blending blood orange, rose, peony, and patchouli for graceful sophistication', 2012, NULL, NULL, 'FLORAL_CHYPRE', 'FEMALE', 'MODERATE', 'LONG_LASTING', 'SPRING_SUMMER', 'CASUAL_EVENING', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', '550e8400-e29b-41d4-a716-446655440002', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Dior Homme Intense', 'Powerful woody aromatic with iris sophistication', 'Intense masculine composition featuring lavender, iris, amber, and cedar for distinguished presence', 2011, NULL, NULL, 'WOODY_FLORAL_MUSK', 'MALE', 'STRONG', 'VERY_LONG_LASTING', 'FALL_WINTER', 'EVENING_BUSINESS', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', '550e8400-e29b-41d4-a716-446655440002', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Jadore', 'Luminous floral bouquet of exceptional femininity', 'Radiant blend of ylang-ylang, Damascus rose, jasmine, and tuberose for timeless elegance', 1999, NULL, NULL, 'FLORAL', 'FEMALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'EVENING_FORMAL', false, false, true, now(), now()),
-- Tom Ford
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', '550e8400-e29b-41d4-a716-446655440003', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae4', 'Black Orchid', 'Luxurious dark fragrance with sensual mysterious character', 'Opulent blend of black truffle, ylang-ylang, black orchid, and patchouli creating intoxicating evening statement', 2006, NULL, NULL, 'ORIENTAL_FLORAL', 'UNISEX', 'HEAVY', 'VERY_LONG_LASTING', 'FALL_WINTER', 'EVENING_SPECIAL', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a10', '550e8400-e29b-41d4-a716-446655440003', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae4', 'Oud Wood', 'Refined woody oriental with rare oud sophistication', 'Luxurious composition featuring oud wood, rosewood, cardamom, and sandalwood for exotic elegance', 2007, NULL, NULL, 'WOODY_ORIENTAL', 'UNISEX', 'MODERATE', 'LONG_LASTING', 'FALL_WINTER', 'EVENING_BUSINESS', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '550e8400-e29b-41d4-a716-446655440003', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae4', 'Tobacco Vanille', 'Rich oriental spicy with tobacco leaf warmth', 'Opulent blend of tobacco leaf, vanilla, cocoa, tonka bean, and dried fruits for luxurious depth', 2007, NULL, NULL, 'ORIENTAL_SPICY', 'UNISEX', 'HEAVY', 'VERY_LONG_LASTING', 'FALL_WINTER', 'EVENING_SPECIAL', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '550e8400-e29b-41d4-a716-446655440003', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae4', 'Lost Cherry', 'Intoxicating cherry almond with sensual depth', 'Addictive blend of black cherry, bitter almond, Turkish rose, and tonka bean', 2018, NULL, NULL, 'ORIENTAL_GOURMAND', 'UNISEX', 'STRONG', 'LONG_LASTING', 'FALL_WINTER', 'EVENING_PARTY', false, false, true, now(), now()),
-- Creed
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', '550e8400-e29b-41d4-a716-446655440004', '6ba7b810-9dad-11d1-80b4-00c04fd430c3', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Aventus', 'Legendary fruity chypre celebrating strength and success', 'Masterful composition featuring pineapple, birch, musk, and oakmoss delivering iconic powerful presence', 2010, NULL, NULL, 'FRUITY_CHYPRE', 'MALE', 'STRONG', 'VERY_LONG_LASTING', 'ALL_SEASONS', 'BUSINESS_EVENING', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', '550e8400-e29b-41d4-a716-446655440004', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Silver Mountain Water', 'Fresh aquatic capturing alpine clarity and coolness', 'Crisp blend of bergamot, tea, blackcurrant, and musk for refreshing vitality', 1995, NULL, NULL, 'AROMATIC_FRESH', 'UNISEX', 'MODERATE', 'LONG_LASTING', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', '550e8400-e29b-41d4-a716-446655440004', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Green Irish Tweed', 'Classic fresh green with aristocratic heritage', 'Timeless masculine scent combining lemon verbena, violet leaves, sandalwood, and ambergris', 1985, NULL, NULL, 'FRESH_GREEN', 'MALE', 'MODERATE', 'LONG_LASTING', 'SPRING_SUMMER', 'BUSINESS_CASUAL', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', '550e8400-e29b-41d4-a716-446655440004', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Viking', 'Bold aromatic fougere with Nordic inspiration', 'Powerful blend of bergamot, pink pepper, rose, and sandalwood for adventurous spirit', 2017, NULL, NULL, 'AROMATIC_FOUGERE', 'MALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'CASUAL_EVENING', false, false, true, now(), now()),
-- YSL
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', '550e8400-e29b-41d4-a716-446655440005', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Black Opium', 'Addictive modern gourmand with coffee and vanilla sweetness', 'Energetic feminine scent combining coffee, white flowers, and vanilla for vibrant youthful allure', 2014, NULL, NULL, 'ORIENTAL_VANILLA', 'FEMALE', 'STRONG', 'LONG_LASTING', 'FALL_WINTER', 'EVENING_PARTY', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', '550e8400-e29b-41d4-a716-446655440005', '6ba7b810-9dad-11d1-80b4-00c04fd430c2', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Mon Paris', 'Sweet floral fruity with romantic Parisian passion', 'Youthful composition combining strawberry, raspberry, pear, peony, and patchouli', 2016, NULL, NULL, 'FLORAL_FRUITY', 'FEMALE', 'STRONG', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_EVENING', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', '550e8400-e29b-41d4-a716-446655440005', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Libre', 'Bold floral fougere celebrating feminine freedom', 'Daring composition blending lavender, orange blossom, vanilla, and musk for empowered femininity', 2019, NULL, NULL, 'FLORAL_FOUGERE', 'FEMALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'CASUAL_EVENING', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', '550e8400-e29b-41d4-a716-446655440005', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'La Nuit de L Homme', 'Sensual cardamom spicy with mysterious allure', 'Seductive blend of cardamom, cedar, lavender, and vetiver for evening sophistication', 2009, NULL, NULL, 'ORIENTAL_SPICY', 'MALE', 'MODERATE', 'MODERATE', 'FALL_WINTER', 'EVENING_CASUAL', false, false, true, now(), now());

-- Hermès
INSERT INTO public.product (id, brand_id, category_id, made_in_id, name, short_description, description, launch_year, image_public_id, image_url, fragrance_family, gender, sillage, longevity, seasonality, occasion, is_limited_edition, is_discontinued, is_active, created_at, updated_at) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', '550e8400-e29b-41d4-a716-446655440006', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Terre d Hermes', 'Woody mineral fragrance expressing connection to earth', 'Sophisticated masculine composition featuring orange, flint, cedar, and vetiver for refined character', 2006, NULL, NULL, 'WOODY_SPICY', 'MALE', 'MODERATE', 'LONG_LASTING', 'ALL_SEASONS', 'BUSINESS_CASUAL', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '550e8400-e29b-41d4-a716-446655440006', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Un Jardin sur le Nil', 'Fresh green vegetal evoking Nile garden serenity', 'Aquatic green blend of grapefruit, lotus, calamus, and sycamore creating peaceful escape', 2005, NULL, NULL, 'FRESH_GREEN', 'UNISEX', 'LIGHT', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', '550e8400-e29b-41d4-a716-446655440006', '6ba7b810-9dad-11d1-80b4-00c04fd430c2', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Eau d Orange Verte', 'Fresh citrus cologne with timeless simplicity', 'Classic unisex blend of orange, lemon, mint, patchouli, and oakmoss for vibrant clarity', 1979, NULL, NULL, 'CITRUS_AROMATIC', 'UNISEX', 'LIGHT', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', '550e8400-e29b-41d4-a716-446655440006', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Twilly d Hermes', 'Playful floral ginger with youthful spirit', 'Vibrant blend of ginger, tuberose, and sandalwood for modern feminine charm', 2017, NULL, NULL, 'FLORAL_ORIENTAL', 'FEMALE', 'MODERATE', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
-- Guerlain
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', '550e8400-e29b-41d4-a716-446655440007', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Shalimar', 'Legendary oriental fragrance with rich vanilla and balsamic warmth', 'Historic masterpiece blending bergamot, iris, vanilla, and tonka bean for timeless sophistication', 1925, NULL, NULL, 'ORIENTAL_VANILLA', 'FEMALE', 'HEAVY', 'VERY_LONG_LASTING', 'FALL_WINTER', 'EVENING_FORMAL', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', '550e8400-e29b-41d4-a716-446655440007', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Mon Guerlain', 'Sophisticated oriental fresh celebrating modern femininity', 'Elegant composition featuring lavender, jasmine, vanilla, and sandalwood for confident grace', 2017, NULL, NULL, 'ORIENTAL_FRESH', 'FEMALE', 'MODERATE', 'LONG_LASTING', 'ALL_SEASONS', 'CASUAL_BUSINESS', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', '550e8400-e29b-41d4-a716-446655440007', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'L Homme Ideal', 'Warm oriental woody with almond sophistication', 'Modern masculine blend of almond, tonka bean, leather, and sandalwood', 2014, NULL, NULL, 'WOODY_ORIENTAL', 'MALE', 'MODERATE', 'LONG_LASTING', 'FALL_WINTER', 'BUSINESS_EVENING', false, false, true, now(), now()),
-- Givenchy
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', '550e8400-e29b-41d4-a716-446655440008', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Gentleman Reserve Privee', 'Elegant woody aromatic with whiskey barrel sophistication', 'Refined masculine blend featuring whiskey accord, iris, and tonka bean', 2019, NULL, NULL, 'WOODY_AROMATIC', 'MALE', 'MODERATE', 'LONG_LASTING', 'FALL_WINTER', 'EVENING_BUSINESS', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a29', '550e8400-e29b-41d4-a716-446655440008', '6ba7b810-9dad-11d1-80b4-00c04fd430c2', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'L Interdit', 'Bold white floral expressing forbidden elegance', 'Daring feminine blend of orange blossom, jasmine, tuberose, and patchouli', 2018, NULL, NULL, 'WHITE_FLORAL', 'FEMALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'EVENING_CASUAL', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a30', '550e8400-e29b-41d4-a716-446655440008', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Irresistible', 'Radiant floral fruity with joyful femininity', 'Luminous composition featuring pear, rose, ambrette, and Virginia cedar', 2020, NULL, NULL, 'FLORAL_FRUITY', 'FEMALE', 'MODERATE', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
-- Versace
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', '550e8400-e29b-41d4-a716-446655440009', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'Eros', 'Fresh oriental masculine fragrance with Mediterranean passion', 'Vibrant composition combining mint, green apple, tonka bean, and vanilla', 2012, NULL, NULL, 'ORIENTAL_FOUGERE', 'MALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'CASUAL_EVENING', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a32', '550e8400-e29b-41d4-a716-446655440009', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'Dylan Blue', 'Fresh aromatic fougere with Mediterranean character', 'Dynamic masculine scent combining bergamot, grapefruit, patchouli, and incense', 2016, NULL, NULL, 'AROMATIC_FOUGERE', 'MALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'CASUAL_BUSINESS', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', '550e8400-e29b-41d4-a716-446655440009', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'Crystal Noir', 'Mysterious oriental floral with dark sensuality', 'Exotic feminine blend of ginger, gardenia, amber, and musk', 2004, NULL, NULL, 'ORIENTAL_FLORAL', 'FEMALE', 'STRONG', 'LONG_LASTING', 'FALL_WINTER', 'EVENING_SPECIAL', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a34', '550e8400-e29b-41d4-a716-446655440009', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'Bright Crystal', 'Fresh floral fruity with luminous transparency', 'Sparkling blend of pomegranate, peony, magnolia, and amber for radiant femininity', 2006, NULL, NULL, 'FLORAL_FRUITY', 'FEMALE', 'MODERATE', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
-- Prada
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a35', '550e8400-e29b-41d4-a716-446655440010', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'La Femme Prada', 'Elegant floral oriental celebrating modern femininity', 'Sophisticated blend of frangipani, vanilla, patchouli, and beeswax', 2016, NULL, NULL, 'FLORAL_ORIENTAL', 'FEMALE', 'MODERATE', 'LONG_LASTING', 'FALL_WINTER', 'EVENING_CASUAL', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a36', '550e8400-e29b-41d4-a716-446655440010', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'Luna Rossa Carbon', 'Modern aromatic fougere with metallic sophistication', 'Contemporary composition featuring lavender, bergamot, coal, and patchouli', 2017, NULL, NULL, 'AROMATIC_FOUGERE', 'MALE', 'MODERATE', 'LONG_LASTING', 'ALL_SEASONS', 'BUSINESS_CASUAL', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a37', '550e8400-e29b-41d4-a716-446655440010', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'L Homme Prada', 'Sophisticated amber fougere with modern elegance', 'Refined blend of iris, amber, neroli, and patchouli', 2016, NULL, NULL, 'AMBER', 'MALE', 'MODERATE', 'LONG_LASTING', 'ALL_SEASONS', 'BUSINESS_CASUAL', false, false, true, now(), now());

-- Burberry
INSERT INTO public.product (id, brand_id, category_id, made_in_id, name, short_description, description, launch_year, image_public_id, image_url, fragrance_family, gender, sillage, longevity, seasonality, occasion, is_limited_edition, is_discontinued, is_active, created_at, updated_at) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a38', '550e8400-e29b-41d4-a716-446655440011', '6ba7b810-9dad-11d1-80b4-00c04fd430c2', '7c9e6679-7425-40de-944b-e07fc1f90ae3', 'Burberry Brit', 'Fresh green floral with youthful British charm', 'Playful feminine scent featuring lime, pear, peony, and vanilla', 2003, NULL, NULL, 'FRESH_FLORAL', 'FEMALE', 'MODERATE', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a39', '550e8400-e29b-41d4-a716-446655440011', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae3', 'My Burberry', 'Floral rain-kissed celebrating contemporary British style', 'Elegant feminine blend of sweet pea, quince, rose, and patchouli', 2014, NULL, NULL, 'FLORAL_FRESH', 'FEMALE', 'MODERATE', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_BUSINESS', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a40', '550e8400-e29b-41d4-a716-446655440011', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae3', 'Burberry Hero', 'Bold woody aromatic with modern heroic masculinity', 'Dynamic blend of bergamot, juniper, black pepper, and cedarwood', 2021, NULL, NULL, 'WOODY_AROMATIC', 'MALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'CASUAL_EVENING', false, false, true, now(), now()),
-- Jo Malone
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a41', '550e8400-e29b-41d4-a716-446655440012', '6ba7b810-9dad-11d1-80b4-00c04fd430c4', '7c9e6679-7425-40de-944b-e07fc1f90ae3', 'Lime Basil and Mandarin', 'Vibrant citrus aromatic cologne with herbal freshness', 'Zesty composition blending lime, basil, thyme, and vetiver', 1999, NULL, NULL, 'CITRUS_AROMATIC', 'UNISEX', 'LIGHT', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a42', '550e8400-e29b-41d4-a716-446655440012', '6ba7b810-9dad-11d1-80b4-00c04fd430c4', '7c9e6679-7425-40de-944b-e07fc1f90ae3', 'Wood Sage and Sea Salt', 'Coastal aromatic capturing windswept minerality', 'Fresh unisex blend of sea salt, sage, grapefruit, and ambrette', 2014, NULL, NULL, 'AROMATIC_AQUATIC', 'UNISEX', 'LIGHT', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a43', '550e8400-e29b-41d4-a716-446655440012', '6ba7b810-9dad-11d1-80b4-00c04fd430c4', '7c9e6679-7425-40de-944b-e07fc1f90ae3', 'English Pear and Freesia', 'Delicate fruity floral with autumnal charm', 'Elegant blend of pear, freesia, rose, and musk', 2010, NULL, NULL, 'FRUITY', 'UNISEX', 'SOFT', 'MODERATE', 'FALL', 'CASUAL_DAYTIME', false, false, true, now(), now()),
-- MFK
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', '550e8400-e29b-41d4-a716-446655440013', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Baccarat Rouge 540', 'Luminous woody floral with amber brilliance', 'Radiant luxury scent combining jasmine, saffron, cedarwood, and ambergris', 2015, NULL, NULL, 'FLORAL_AMBER', 'UNISEX', 'STRONG', 'VERY_LONG_LASTING', 'ALL_SEASONS', 'EVENING_SPECIAL', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a45', '550e8400-e29b-41d4-a716-446655440013', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Aqua Universalis', 'Pure luminous fresh with aquatic transparency', 'Crystalline composition of bergamot, lemon, lily of the valley, and musk', 2009, NULL, NULL, 'FRESH_AQUATIC', 'UNISEX', 'MODERATE', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a46', '550e8400-e29b-41d4-a716-446655440013', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Grand Soir', 'Opulent amber vanilla with sophisticated warmth', 'Luxurious blend of benzoin, tonka, amber, and vanilla', 2016, NULL, NULL, 'AMBER', 'UNISEX', 'HEAVY', 'VERY_LONG_LASTING', 'FALL_WINTER', 'EVENING_FORMAL', false, false, true, now(), now()),
-- Byredo
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a47', '550e8400-e29b-41d4-a716-446655440014', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae6', 'Gypsy Water', 'Woody aromatic evoking nomadic forest romance', 'Bohemian blend of pine needles, lemon, incense, vanilla, and sandalwood', 2008, NULL, NULL, 'WOODY_AROMATIC', 'UNISEX', 'MODERATE', 'MODERATE', 'ALL_SEASONS', 'CASUAL_DAYTIME', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a48', '550e8400-e29b-41d4-a716-446655440014', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae6', 'Bal d Afrique', 'Warm floral woody celebrating African romance', 'Vibrant blend of African marigold, neroli, cyclamen, vetiver, and cedarwood', 2009, NULL, NULL, 'FLORAL_WOODY', 'UNISEX', 'MODERATE', 'LONG_LASTING', 'ALL_SEASONS', 'CASUAL_EVENING', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a49', '550e8400-e29b-41d4-a716-446655440014', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae6', 'Mojave Ghost', 'Ethereal woody floral with desert mystique', 'Delicate blend of ambrette, magnolia, sandalwood, and cedar', 2014, NULL, NULL, 'FLORAL_WOODY', 'UNISEX', 'MODERATE', 'LONG_LASTING', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
-- Le Labo
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a50', '550e8400-e29b-41d4-a716-446655440015', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae4', 'Santal 33', 'Iconic woody spicy fragrance with creamy sandalwood soul', 'Cult favorite combining sandalwood, cardamom, iris, violet, and leather', 2011, NULL, NULL, 'WOODY_SPICY', 'UNISEX', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'CASUAL_EVENING', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a51', '550e8400-e29b-41d4-a716-446655440015', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae4', 'Another 13', 'Avant-garde musk with synthetic transparency', 'Minimalist composition featuring ambroxan, musk, jasmine, and moss', 2010, NULL, NULL, 'WOODY_MUSK', 'UNISEX', 'MODERATE', 'LONG_LASTING', 'ALL_SEASONS', 'CASUAL_EVENING', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a52', '550e8400-e29b-41d4-a716-446655440015', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae4', 'Rose 31', 'Bold woody rose with masculine edge', 'Distinctive blend of rose, cumin, cedar, and gaiac wood', 2006, NULL, NULL, 'FLORAL_WOODY', 'UNISEX', 'MODERATE', 'LONG_LASTING', 'ALL_SEASONS', 'CASUAL_BUSINESS', false, false, true, now(), now());

-- Dolce & Gabbana
INSERT INTO public.product (id, brand_id, category_id, made_in_id, name, short_description, description, launch_year, image_public_id, image_url, fragrance_family, gender, sillage, longevity, seasonality, occasion, is_limited_edition, is_discontinued, is_active, created_at, updated_at) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a53', '550e8400-e29b-41d4-a716-446655440016', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'Light Blue', 'Fresh citrus aromatic with Sicilian summer spirit', 'Vibrant blend of Sicilian lemon, apple, jasmine, and cedarwood', 2001, NULL, NULL, 'CITRUS_AROMATIC', 'FEMALE', 'MODERATE', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a54', '550e8400-e29b-41d4-a716-446655440016', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'The One', 'Warm oriental floral with sophisticated allure', 'Seductive blend of mandarin, peach, jasmine, and vanilla', 2006, NULL, NULL, 'ORIENTAL_FLORAL', 'FEMALE', 'MODERATE', 'LONG_LASTING', 'FALL_WINTER', 'EVENING_CASUAL', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', '550e8400-e29b-41d4-a716-446655440016', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'K by Dolce Gabbana', 'Bold aromatic woody with regal masculinity', 'Refined blend of blood orange, juniper, cedarwood, and vetiver', 2019, NULL, NULL, 'WOODY_AROMATIC', 'MALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'BUSINESS_CASUAL', false, false, true, now(), now()),
-- Giorgio Armani
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a56', '550e8400-e29b-41d4-a716-446655440017', '6ba7b810-9dad-11d1-80b4-00c04fd430c2', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'Acqua di Gio', 'Fresh aquatic aromatic capturing Mediterranean essence', 'Iconic blend of bergamot, neroli, patchouli, and aquatic notes', 1996, NULL, NULL, 'AROMATIC_AQUATIC', 'MALE', 'MODERATE', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a57', '550e8400-e29b-41d4-a716-446655440017', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'Si', 'Chic chypre with sophisticated feminine elegance', 'Modern blend of blackcurrant, rose, vanilla, and patchouli', 2013, NULL, NULL, 'FLORAL_CHYPRE', 'FEMALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'EVENING_BUSINESS', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a58', '550e8400-e29b-41d4-a716-446655440017', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'Code', 'Warm oriental spicy with seductive masculinity', 'Sophisticated blend of bergamot, star anise, tonka bean, and guaiac wood', 2004, NULL, NULL, 'ORIENTAL_SPICY', 'MALE', 'STRONG', 'LONG_LASTING', 'FALL_WINTER', 'EVENING_CASUAL', false, false, true, now(), now()),
-- Carolina Herrera
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a59', '550e8400-e29b-41d4-a716-446655440018', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae7', 'Good Girl', 'Sensual oriental floral with playful duality', 'Intriguing blend of almond, coffee, tuberose, and tonka bean', 2016, NULL, NULL, 'ORIENTAL_FLORAL', 'FEMALE', 'STRONG', 'LONG_LASTING', 'FALL_WINTER', 'EVENING_PARTY', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a60', '550e8400-e29b-41d4-a716-446655440018', '6ba7b810-9dad-11d1-80b4-00c04fd430c2', '7c9e6679-7425-40de-944b-e07fc1f90ae7', '212 VIP', 'Sparkling floral woody with urban sophistication', 'Glamorous blend of passion fruit, gardenia, musk, and benzoin', 2010, NULL, NULL, 'FLORAL_WOODY', 'FEMALE', 'MODERATE', 'MODERATE', 'ALL_SEASONS', 'EVENING_PARTY', false, false, true, now(), now()),
-- Acqua di Parma
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a61', '550e8400-e29b-41d4-a716-446655440019', '6ba7b810-9dad-11d1-80b4-00c04fd430c4', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'Colonia', 'Classic Italian citrus cologne with timeless elegance', 'Refined blend of Sicilian citrus, lavender, rosemary, and vetiver', 1916, NULL, NULL, 'CITRUS_AROMATIC', 'UNISEX', 'LIGHT', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a62', '550e8400-e29b-41d4-a716-446655440019', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae2', 'Oud', 'Rich woody oriental with noble oud depth', 'Opulent blend of oud, sandalwood, leather, and amber', 2012, NULL, NULL, 'WOODY_ORIENTAL', 'UNISEX', 'HEAVY', 'VERY_LONG_LASTING', 'FALL_WINTER', 'EVENING_FORMAL', false, false, true, now(), now()),
-- Parfums de Marly
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a63', '550e8400-e29b-41d4-a716-446655440020', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Layton', 'Rich gourmand aromatic with sophisticated warmth', 'Exquisite blend of apple, bergamot, vanilla, and sandalwood', 2016, NULL, NULL, 'ORIENTAL_GOURMAND', 'MALE', 'STRONG', 'VERY_LONG_LASTING', 'FALL_WINTER', 'EVENING_BUSINESS', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a64', '550e8400-e29b-41d4-a716-446655440020', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Delina', 'Romantic floral fruity with feminine grace', 'Enchanting blend of Turkish rose, lychee, vanilla, and musk', 2017, NULL, NULL, 'FLORAL_FRUITY', 'FEMALE', 'STRONG', 'LONG_LASTING', 'ALL_SEASONS', 'CASUAL_EVENING', false, false, true, now(), now()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a65', '550e8400-e29b-41d4-a716-446655440020', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Pegasus', 'Elegant almond vanilla with powdery sophistication', 'Luxurious blend of bitter almond, jasmine, vanilla, and sandalwood', 2011, NULL, NULL, 'ORIENTAL_VANILLA', 'MALE', 'STRONG', 'VERY_LONG_LASTING', 'FALL_WINTER', 'EVENING_FORMAL', false, false, true, now(), now()),
-- Tester for demo
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', '550e8400-e29b-41d4-a716-446655440001', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '7c9e6679-7425-40de-944b-e07fc1f90ae1', 'Temp Tester', 'Budget-friendly tester for payment QA', 'Purpose-built affordable fragrance to verify checkout and PayOS settlement flows', 2025, NULL, NULL, 'CITRUS_AROMATIC', 'UNISEX', 'LIGHT', 'MODERATE', 'SPRING_SUMMER', 'CASUAL_DAYTIME', false, false, true, now(), now());

-- =============================================
-- PRODUCT VARIANTS (Multiple sizes for each product)
-- =============================================
INSERT INTO public.product_variant (id, product_id, variant_sku, volume_ml, package_type, price, compare_at_price, currency_code, stock_quantity, low_stock_threshold, image_public_id, image_url, is_active, created_at, updated_at) VALUES
-- Chanel No. 5 variants
('b1234567-89ab-4cde-f012-345678901001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'CHANEL-NO5-35ML-EDP', 35.00, 'Spray Bottle', 3200000, 3500000, 'VND', 45, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'CHANEL-NO5-50ML-EDP', 50.00, 'Spray Bottle', 3850000, 4200000, 'VND', 38, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'CHANEL-NO5-100ML-EDP', 100.00, 'Spray Bottle', 5950000, 6500000, 'VND', 25, 8, NULL, NULL, true, now(), now()),
-- Coco Mademoiselle variants
('b1234567-89ab-4cde-f012-345678901004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'CHANEL-COCO-35ML-EDP', 35.00, 'Spray Bottle', 3100000, 3400000, 'VND', 52, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901005', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'CHANEL-COCO-50ML-EDP', 50.00, 'Spray Bottle', 3700000, 4100000, 'VND', 48, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901006', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'CHANEL-COCO-100ML-EDP', 100.00, 'Spray Bottle', 5800000, 6400000, 'VND', 32, 8, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901007', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'CHANEL-COCO-200ML-EDP', 200.00, 'Spray Bottle', 9500000, 10500000, 'VND', 12, 3, NULL, NULL, true, now(), now()),
-- Bleu de Chanel variants
('b1234567-89ab-4cde-f012-345678901008', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'CHANEL-BLEU-50ML-EDP', 50.00, 'Spray Bottle', 3600000, 4000000, 'VND', 61, 14, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901009', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'CHANEL-BLEU-100ML-EDP', 100.00, 'Spray Bottle', 5600000, 6200000, 'VND', 43, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901010', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'CHANEL-BLEU-150ML-EDP', 150.00, 'Spray Bottle', 7900000, 8700000, 'VND', 22, 6, NULL, NULL, true, now(), now()),
-- Chance Eau Tendre variants
('b1234567-89ab-4cde-f012-345678901011', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'CHANEL-CHANCE-50ML-EDP', 50.00, 'Spray Bottle', 3400000, 3800000, 'VND', 55, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901012', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'CHANEL-CHANCE-100ML-EDP', 100.00, 'Spray Bottle', 5200000, 5700000, 'VND', 38, 8, NULL, NULL, true, now(), now()),
-- Dior Sauvage variants
('b1234567-89ab-4cde-f012-345678901013', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'DIOR-SAUVAGE-60ML-EDP', 60.00, 'Spray Bottle', 3200000, 3500000, 'VND', 78, 15, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901014', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'DIOR-SAUVAGE-100ML-EDP', 100.00, 'Spray Bottle', 4750000, 5200000, 'VND', 56, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901015', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'DIOR-SAUVAGE-200ML-EDP', 200.00, 'Spray Bottle', 7800000, 8500000, 'VND', 18, 5, NULL, NULL, true, now(), now()),
-- Miss Dior variants
('b1234567-89ab-4cde-f012-345678901016', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'DIOR-MISS-50ML-EDP', 50.00, 'Spray Bottle', 3300000, 3600000, 'VND', 64, 15, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901017', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'DIOR-MISS-100ML-EDP', 100.00, 'Spray Bottle', 5100000, 5600000, 'VND', 42, 10, NULL, NULL, true, now(), now()),
-- Dior Homme Intense variants
('b1234567-89ab-4cde-f012-345678901018', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'DIOR-HOMME-50ML-EDP', 50.00, 'Spray Bottle', 3400000, 3800000, 'VND', 48, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901019', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'DIOR-HOMME-100ML-EDP', 100.00, 'Spray Bottle', 5200000, 5700000, 'VND', 31, 8, NULL, NULL, true, now(), now()),
-- Jadore variants
('b1234567-89ab-4cde-f012-345678901020', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'DIOR-JADORE-30ML-EDP', 30.00, 'Spray Bottle', 2600000, 2900000, 'VND', 65, 15, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901021', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'DIOR-JADORE-50ML-EDP', 50.00, 'Spray Bottle', 3500000, 3900000, 'VND', 52, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901022', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'DIOR-JADORE-100ML-EDP', 100.00, 'Spray Bottle', 5400000, 6000000, 'VND', 35, 8, NULL, NULL, true, now(), now());

-- Tom Ford Black Orchid variants
INSERT INTO public.product_variant (id, product_id, variant_sku, volume_ml, package_type, price, compare_at_price, currency_code, stock_quantity, low_stock_threshold, image_public_id, image_url, is_active, created_at, updated_at) VALUES
('b1234567-89ab-4cde-f012-345678901023', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'TF-BLACKORCH-50ML-EDP', 50.00, 'Spray Bottle', 5400000, 5900000, 'VND', 34, 8, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901024', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'TF-BLACKORCH-100ML-EDP', 100.00, 'Spray Bottle', 8900000, 9700000, 'VND', 19, 5, NULL, NULL, true, now(), now()),
-- Tom Ford Oud Wood variants
('b1234567-89ab-4cde-f012-345678901025', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 'TF-OUDWOOD-50ML-EDP', 50.00, 'Spray Bottle', 5700000, 6300000, 'VND', 29, 6, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901026', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 'TF-OUDWOOD-100ML-EDP', 100.00, 'Spray Bottle', 9400000, 10300000, 'VND', 16, 4, NULL, NULL, true, now(), now()),
-- Tom Ford Tobacco Vanille variants
('b1234567-89ab-4cde-f012-345678901027', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'TF-TOBACCO-50ML-EDP', 50.00, 'Spray Bottle', 5900000, 6500000, 'VND', 27, 6, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901028', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'TF-TOBACCO-100ML-EDP', 100.00, 'Spray Bottle', 9700000, 10700000, 'VND', 14, 3, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901029', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'TF-TOBACCO-250ML-EDP', 250.00, 'Spray Bottle', 19800000, 21800000, 'VND', 5, 2, NULL, NULL, true, now(), now()),
-- Tom Ford Lost Cherry variants
('b1234567-89ab-4cde-f012-345678901030', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'TF-LOSTCHERRY-50ML-EDP', 50.00, 'Spray Bottle', 6100000, 6700000, 'VND', 25, 6, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901031', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'TF-LOSTCHERRY-100ML-EDP', 100.00, 'Spray Bottle', 9800000, 10800000, 'VND', 12, 3, NULL, NULL, true, now(), now()),
-- Creed Aventus variants
('b1234567-89ab-4cde-f012-345678901032', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'CREED-AVENTUS-50ML-PARF', 50.00, 'Spray Bottle', 9200000, 10000000, 'VND', 28, 5, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901033', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'CREED-AVENTUS-100ML-PARF', 100.00, 'Spray Bottle', 14500000, 16000000, 'VND', 15, 3, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901034', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'CREED-AVENTUS-250ML-PARF', 250.00, 'Spray Bottle', 28000000, 31000000, 'VND', 6, 2, NULL, NULL, true, now(), now()),
-- Creed Silver Mountain Water variants
('b1234567-89ab-4cde-f012-345678901035', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'CREED-SILVER-50ML-EDP', 50.00, 'Spray Bottle', 8800000, 9600000, 'VND', 31, 6, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901036', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'CREED-SILVER-100ML-EDP', 100.00, 'Spray Bottle', 13900000, 15300000, 'VND', 17, 4, NULL, NULL, true, now(), now()),
-- Creed Green Irish Tweed variants
('b1234567-89ab-4cde-f012-345678901037', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'CREED-GREEN-50ML-EDP', 50.00, 'Spray Bottle', 8600000, 9400000, 'VND', 34, 8, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901038', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'CREED-GREEN-100ML-EDP', 100.00, 'Spray Bottle', 13600000, 15000000, 'VND', 19, 4, NULL, NULL, true, now(), now()),
-- Creed Viking variants
('b1234567-89ab-4cde-f012-345678901039', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'CREED-VIKING-50ML-EDP', 50.00, 'Spray Bottle', 8700000, 9500000, 'VND', 28, 6, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901040', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'CREED-VIKING-100ML-EDP', 100.00, 'Spray Bottle', 13800000, 15200000, 'VND', 15, 4, NULL, NULL, true, now(), now());

-- YSL Black Opium variants
INSERT INTO public.product_variant (id, product_id, variant_sku, volume_ml, package_type, price, compare_at_price, currency_code, stock_quantity, low_stock_threshold, image_public_id, image_url, is_active, created_at, updated_at) VALUES
('b1234567-89ab-4cde-f012-345678901041', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'YSL-BLACKOP-50ML-EDP', 50.00, 'Spray Bottle', 3100000, 3400000, 'VND', 67, 15, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901042', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'YSL-BLACKOP-90ML-EDP', 90.00, 'Spray Bottle', 4800000, 5300000, 'VND', 43, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901043', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'YSL-BLACKOP-150ML-EDP', 150.00, 'Spray Bottle', 6900000, 7600000, 'VND', 18, 5, NULL, NULL, true, now(), now()),
-- YSL Mon Paris variants
('b1234567-89ab-4cde-f012-345678901044', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'YSL-MONPAR-30ML-EDT', 30.00, 'Spray Bottle', 2200000, 2500000, 'VND', 76, 18, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901045', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'YSL-MONPAR-50ML-EDT', 50.00, 'Spray Bottle', 3000000, 3300000, 'VND', 58, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901046', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'YSL-MONPAR-90ML-EDT', 90.00, 'Spray Bottle', 4400000, 4900000, 'VND', 35, 8, NULL, NULL, true, now(), now()),
-- YSL Libre variants
('b1234567-89ab-4cde-f012-345678901047', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'YSL-LIBRE-30ML-EDP', 30.00, 'Spray Bottle', 2400000, 2700000, 'VND', 82, 18, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901048', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'YSL-LIBRE-50ML-EDP', 50.00, 'Spray Bottle', 3300000, 3600000, 'VND', 64, 14, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901049', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'YSL-LIBRE-90ML-EDP', 90.00, 'Spray Bottle', 5100000, 5600000, 'VND', 41, 10, NULL, NULL, true, now(), now()),
-- YSL La Nuit de L Homme variants
('b1234567-89ab-4cde-f012-345678901050', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 'YSL-LANUIT-60ML-EDP', 60.00, 'Spray Bottle', 2800000, 3100000, 'VND', 55, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901051', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 'YSL-LANUIT-100ML-EDP', 100.00, 'Spray Bottle', 4200000, 4600000, 'VND', 38, 8, NULL, NULL, true, now(), now()),
-- Hermès Terre d Hermes variants
('b1234567-89ab-4cde-f012-345678901052', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'HERMES-TERRE-50ML-EDP', 50.00, 'Spray Bottle', 3400000, 3700000, 'VND', 52, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901053', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'HERMES-TERRE-100ML-EDP', 100.00, 'Spray Bottle', 5200000, 5700000, 'VND', 38, 8, NULL, NULL, true, now(), now()),
-- Hermès Un Jardin sur le Nil variants
('b1234567-89ab-4cde-f012-345678901054', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'HERMES-JARDIN-50ML-EDT', 50.00, 'Spray Bottle', 2900000, 3200000, 'VND', 46, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901055', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'HERMES-JARDIN-100ML-EDT', 100.00, 'Spray Bottle', 4600000, 5100000, 'VND', 28, 6, NULL, NULL, true, now(), now()),
-- Hermès Eau d Orange Verte variants
('b1234567-89ab-4cde-f012-345678901056', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'HERMES-ORANGE-50ML-EDT', 50.00, 'Spray Bottle', 2700000, 3000000, 'VND', 57, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901057', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'HERMES-ORANGE-100ML-EDT', 100.00, 'Spray Bottle', 4200000, 4600000, 'VND', 38, 8, NULL, NULL, true, now(), now()),
-- Hermès Twilly d Hermes variants
('b1234567-89ab-4cde-f012-345678901058', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', 'HERMES-TWILLY-50ML-EDP', 50.00, 'Spray Bottle', 3000000, 3300000, 'VND', 48, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901059', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', 'HERMES-TWILLY-85ML-EDP', 85.00, 'Spray Bottle', 4500000, 5000000, 'VND', 32, 8, NULL, NULL, true, now(), now());

-- Guerlain Shalimar variants
INSERT INTO public.product_variant (id, product_id, variant_sku, volume_ml, package_type, price, compare_at_price, currency_code, stock_quantity, low_stock_threshold, image_public_id, image_url, is_active, created_at, updated_at) VALUES
('b1234567-89ab-4cde-f012-345678901060', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', 'GUER-SHAL-50ML-EDP', 50.00, 'Spray Bottle', 3600000, 4000000, 'VND', 41, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901061', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', 'GUER-SHAL-90ML-EDP', 90.00, 'Spray Bottle', 5800000, 6400000, 'VND', 27, 6, NULL, NULL, true, now(), now()),
-- Guerlain Mon Guerlain variants
('b1234567-89ab-4cde-f012-345678901062', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', 'GUER-MONGUER-50ML-EDP', 50.00, 'Spray Bottle', 3400000, 3800000, 'VND', 53, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901063', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', 'GUER-MONGUER-100ML-EDP', 100.00, 'Spray Bottle', 5300000, 5800000, 'VND', 34, 8, NULL, NULL, true, now(), now()),
-- Guerlain L Homme Ideal variants
('b1234567-89ab-4cde-f012-345678901064', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', 'GUER-IDEAL-50ML-EDP', 50.00, 'Spray Bottle', 3300000, 3700000, 'VND', 50, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901065', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', 'GUER-IDEAL-100ML-EDP', 100.00, 'Spray Bottle', 5100000, 5600000, 'VND', 32, 8, NULL, NULL, true, now(), now()),
-- Givenchy Gentleman Reserve Privee variants
('b1234567-89ab-4cde-f012-345678901066', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', 'GVCH-GENT-60ML-EDP', 60.00, 'Spray Bottle', 3250000, 3600000, 'VND', 49, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901067', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', 'GVCH-GENT-100ML-EDP', 100.00, 'Spray Bottle', 4900000, 5400000, 'VND', 31, 8, NULL, NULL, true, now(), now()),
-- Givenchy L Interdit variants
('b1234567-89ab-4cde-f012-345678901068', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a29', 'GVCH-INTER-50ML-EDT', 50.00, 'Spray Bottle', 2800000, 3100000, 'VND', 68, 15, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901069', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a29', 'GVCH-INTER-80ML-EDT', 80.00, 'Spray Bottle', 4100000, 4500000, 'VND', 42, 10, NULL, NULL, true, now(), now()),
-- Givenchy Irresistible variants
('b1234567-89ab-4cde-f012-345678901070', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a30', 'GVCH-IRRES-50ML-EDP', 50.00, 'Spray Bottle', 3000000, 3300000, 'VND', 66, 15, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901071', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a30', 'GVCH-IRRES-80ML-EDP', 80.00, 'Spray Bottle', 4500000, 5000000, 'VND', 44, 10, NULL, NULL, true, now(), now()),
-- Versace Eros variants
('b1234567-89ab-4cde-f012-345678901072', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', 'VERS-EROS-50ML-EDP', 50.00, 'Spray Bottle', 2800000, 3100000, 'VND', 63, 15, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901073', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', 'VERS-EROS-100ML-EDP', 100.00, 'Spray Bottle', 4200000, 4600000, 'VND', 44, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901074', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', 'VERS-EROS-200ML-EDP', 200.00, 'Spray Bottle', 6500000, 7200000, 'VND', 18, 5, NULL, NULL, true, now(), now()),
-- Versace Dylan Blue variants
('b1234567-89ab-4cde-f012-345678901075', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a32', 'VERS-DYLAN-50ML-EDT', 50.00, 'Spray Bottle', 2600000, 2900000, 'VND', 72, 16, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901076', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a32', 'VERS-DYLAN-100ML-EDT', 100.00, 'Spray Bottle', 3900000, 4300000, 'VND', 51, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901077', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a32', 'VERS-DYLAN-200ML-EDT', 200.00, 'Spray Bottle', 6100000, 6700000, 'VND', 24, 6, NULL, NULL, true, now(), now()),
-- Versace Crystal Noir variants
('b1234567-89ab-4cde-f012-345678901078', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'VERS-CRYSTAL-50ML-EDT', 50.00, 'Spray Bottle', 2900000, 3200000, 'VND', 53, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901079', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'VERS-CRYSTAL-90ML-EDT', 90.00, 'Spray Bottle', 4300000, 4700000, 'VND', 36, 8, NULL, NULL, true, now(), now()),
-- Versace Bright Crystal variants
('b1234567-89ab-4cde-f012-345678901080', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a34', 'VERS-BRIGHT-50ML-EDT', 50.00, 'Spray Bottle', 2500000, 2800000, 'VND', 68, 15, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901081', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a34', 'VERS-BRIGHT-90ML-EDT', 90.00, 'Spray Bottle', 3800000, 4200000, 'VND', 45, 10, NULL, NULL, true, now(), now());

-- Prada La Femme variants
INSERT INTO public.product_variant (id, product_id, variant_sku, volume_ml, package_type, price, compare_at_price, currency_code, stock_quantity, low_stock_threshold, image_public_id, image_url, is_active, created_at, updated_at) VALUES
('b1234567-89ab-4cde-f012-345678901082', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a35', 'PRADA-FEMME-50ML-EDP', 50.00, 'Spray Bottle', 3500000, 3900000, 'VND', 37, 8, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901083', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a35', 'PRADA-FEMME-100ML-EDP', 100.00, 'Spray Bottle', 5400000, 5900000, 'VND', 22, 5, NULL, NULL, true, now(), now()),
-- Prada Luna Rossa Carbon variants
('b1234567-89ab-4cde-f012-345678901084', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a36', 'PRADA-LUNA-50ML-EDP', 50.00, 'Spray Bottle', 3200000, 3500000, 'VND', 45, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901085', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a36', 'PRADA-LUNA-100ML-EDP', 100.00, 'Spray Bottle', 4900000, 5400000, 'VND', 29, 6, NULL, NULL, true, now(), now()),
-- Prada L Homme variants
('b1234567-89ab-4cde-f012-345678901086', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a37', 'PRADA-HOMME-50ML-EDP', 50.00, 'Spray Bottle', 3100000, 3400000, 'VND', 42, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901087', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a37', 'PRADA-HOMME-100ML-EDP', 100.00, 'Spray Bottle', 4700000, 5200000, 'VND', 28, 6, NULL, NULL, true, now(), now()),
-- Burberry Brit variants
('b1234567-89ab-4cde-f012-345678901088', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a38', 'BURB-BRIT-50ML-EDT', 50.00, 'Spray Bottle', 2400000, 2700000, 'VND', 54, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901089', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a38', 'BURB-BRIT-100ML-EDT', 100.00, 'Spray Bottle', 3600000, 4000000, 'VND', 39, 8, NULL, NULL, true, now(), now()),
-- Burberry My Burberry variants
('b1234567-89ab-4cde-f012-345678901090', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a39', 'BURB-MYBURB-50ML-EDP', 50.00, 'Spray Bottle', 3100000, 3400000, 'VND', 56, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901091', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a39', 'BURB-MYBURB-90ML-EDP', 90.00, 'Spray Bottle', 4700000, 5200000, 'VND', 37, 8, NULL, NULL, true, now(), now()),
-- Burberry Hero variants
('b1234567-89ab-4cde-f012-345678901092', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a40', 'BURB-HERO-50ML-EDP', 50.00, 'Spray Bottle', 2900000, 3200000, 'VND', 48, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901093', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a40', 'BURB-HERO-100ML-EDP', 100.00, 'Spray Bottle', 4400000, 4900000, 'VND', 32, 8, NULL, NULL, true, now(), now()),
-- Jo Malone Lime Basil and Mandarin variants
('b1234567-89ab-4cde-f012-345678901094', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a41', 'JOMALONE-LIME-30ML-COL', 30.00, 'Cologne Bottle', 2100000, 2400000, 'VND', 71, 15, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901095', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a41', 'JOMALONE-LIME-100ML-COL', 100.00, 'Cologne Bottle', 4500000, 5000000, 'VND', 48, 10, NULL, NULL, true, now(), now()),
-- Jo Malone Wood Sage and Sea Salt variants
('b1234567-89ab-4cde-f012-345678901096', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a42', 'JOMALONE-WOOD-30ML-COL', 30.00, 'Cologne Bottle', 2300000, 2600000, 'VND', 69, 15, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901097', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a42', 'JOMALONE-WOOD-100ML-COL', 100.00, 'Cologne Bottle', 4700000, 5200000, 'VND', 44, 10, NULL, NULL, true, now(), now()),
-- Jo Malone English Pear and Freesia variants
('b1234567-89ab-4cde-f012-345678901098', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a43', 'JOMALONE-PEAR-30ML-COL', 30.00, 'Cologne Bottle', 2200000, 2500000, 'VND', 65, 14, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901099', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a43', 'JOMALONE-PEAR-100ML-COL', 100.00, 'Cologne Bottle', 4600000, 5100000, 'VND', 42, 10, NULL, NULL, true, now(), now());

-- MFK Baccarat Rouge 540 variants
INSERT INTO public.product_variant (id, product_id, variant_sku, volume_ml, package_type, price, compare_at_price, currency_code, stock_quantity, low_stock_threshold, image_public_id, image_url, is_active, created_at, updated_at) VALUES
('b1234567-89ab-4cde-f012-345678901100', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'MFK-BACC540-35ML-EDP', 35.00, 'Spray Bottle', 6500000, 7200000, 'VND', 35, 8, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901101', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'MFK-BACC540-70ML-EDP', 70.00, 'Spray Bottle', 8900000, 9800000, 'VND', 26, 5, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901102', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'MFK-BACC540-200ML-EDP', 200.00, 'Spray Bottle', 19500000, 21500000, 'VND', 8, 2, NULL, NULL, true, now(), now()),
-- MFK Aqua Universalis variants
('b1234567-89ab-4cde-f012-345678901103', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a45', 'MFK-AQUA-70ML-EDP', 70.00, 'Spray Bottle', 7800000, 8600000, 'VND', 32, 8, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901104', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a45', 'MFK-AQUA-200ML-EDP', 200.00, 'Spray Bottle', 17900000, 19700000, 'VND', 11, 3, NULL, NULL, true, now(), now()),
-- MFK Grand Soir variants
('b1234567-89ab-4cde-f012-345678901105', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a46', 'MFK-GRAND-70ML-EDP', 70.00, 'Spray Bottle', 8200000, 9000000, 'VND', 28, 6, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901106', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a46', 'MFK-GRAND-200ML-EDP', 200.00, 'Spray Bottle', 18500000, 20400000, 'VND', 9, 2, NULL, NULL, true, now(), now()),
-- Byredo Gypsy Water variants
('b1234567-89ab-4cde-f012-345678901107', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a47', 'BYREDO-GYPSY-50ML-EDP', 50.00, 'Spray Bottle', 6200000, 6800000, 'VND', 33, 8, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901108', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a47', 'BYREDO-GYPSY-100ML-EDP', 100.00, 'Spray Bottle', 9800000, 10800000, 'VND', 19, 5, NULL, NULL, true, now(), now()),
-- Byredo Bal d Afrique variants
('b1234567-89ab-4cde-f012-345678901109', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a48', 'BYREDO-BAL-50ML-EDP', 50.00, 'Spray Bottle', 6400000, 7000000, 'VND', 28, 6, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901110', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a48', 'BYREDO-BAL-100ML-EDP', 100.00, 'Spray Bottle', 10100000, 11100000, 'VND', 16, 4, NULL, NULL, true, now(), now()),
-- Byredo Mojave Ghost variants
('b1234567-89ab-4cde-f012-345678901111', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a49', 'BYREDO-MOJAVE-50ML-EDP', 50.00, 'Spray Bottle', 6300000, 6900000, 'VND', 30, 7, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901112', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a49', 'BYREDO-MOJAVE-100ML-EDP', 100.00, 'Spray Bottle', 9900000, 10900000, 'VND', 17, 4, NULL, NULL, true, now(), now()),
-- Le Labo Santal 33 variants
('b1234567-89ab-4cde-f012-345678901113', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a50', 'LELABO-SANT33-50ML-EDP', 50.00, 'Spray Bottle', 6500000, 7200000, 'VND', 41, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901114', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a50', 'LELABO-SANT33-100ML-EDP', 100.00, 'Spray Bottle', 10200000, 11200000, 'VND', 24, 5, NULL, NULL, true, now(), now()),
-- Le Labo Another 13 variants
('b1234567-89ab-4cde-f012-345678901115', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a51', 'LELABO-ANOTH13-50ML-EDP', 50.00, 'Spray Bottle', 6300000, 6900000, 'VND', 39, 8, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901116', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a51', 'LELABO-ANOTH13-100ML-EDP', 100.00, 'Spray Bottle', 9900000, 10900000, 'VND', 22, 5, NULL, NULL, true, now(), now()),
-- Le Labo Rose 31 variants
('b1234567-89ab-4cde-f012-345678901117', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a52', 'LELABO-ROSE31-50ML-EDP', 50.00, 'Spray Bottle', 6400000, 7000000, 'VND', 35, 8, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901118', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a52', 'LELABO-ROSE31-100ML-EDP', 100.00, 'Spray Bottle', 10000000, 11000000, 'VND', 20, 5, NULL, NULL, true, now(), now());

-- D&G Light Blue variants
INSERT INTO public.product_variant (id, product_id, variant_sku, volume_ml, package_type, price, compare_at_price, currency_code, stock_quantity, low_stock_threshold, image_public_id, image_url, is_active, created_at, updated_at) VALUES
('b1234567-89ab-4cde-f012-345678901119', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a53', 'DG-LIGHTBLUE-50ML-EDP', 50.00, 'Spray Bottle', 2600000, 2900000, 'VND', 62, 14, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901120', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a53', 'DG-LIGHTBLUE-100ML-EDP', 100.00, 'Spray Bottle', 4000000, 4400000, 'VND', 45, 10, NULL, NULL, true, now(), now()),
-- D&G The One variants
('b1234567-89ab-4cde-f012-345678901121', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a54', 'DG-THEONE-50ML-EDP', 50.00, 'Spray Bottle', 2800000, 3100000, 'VND', 55, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901122', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a54', 'DG-THEONE-100ML-EDP', 100.00, 'Spray Bottle', 4300000, 4700000, 'VND', 38, 8, NULL, NULL, true, now(), now()),
-- D&G K by Dolce Gabbana variants
('b1234567-89ab-4cde-f012-345678901123', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'DG-K-50ML-EDP', 50.00, 'Spray Bottle', 2900000, 3200000, 'VND', 48, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901124', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'DG-K-100ML-EDP', 100.00, 'Spray Bottle', 4500000, 5000000, 'VND', 32, 8, NULL, NULL, true, now(), now()),
-- Armani Acqua di Gio variants
('b1234567-89ab-4cde-f012-345678901125', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a56', 'ARMANI-ADG-50ML-EDT', 50.00, 'Spray Bottle', 2500000, 2800000, 'VND', 72, 16, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901126', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a56', 'ARMANI-ADG-100ML-EDT', 100.00, 'Spray Bottle', 3800000, 4200000, 'VND', 55, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901127', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a56', 'ARMANI-ADG-200ML-EDT', 200.00, 'Spray Bottle', 5900000, 6500000, 'VND', 25, 6, NULL, NULL, true, now(), now()),
-- Armani Si variants
('b1234567-89ab-4cde-f012-345678901128', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a57', 'ARMANI-SI-50ML-EDP', 50.00, 'Spray Bottle', 3200000, 3500000, 'VND', 58, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901129', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a57', 'ARMANI-SI-100ML-EDP', 100.00, 'Spray Bottle', 4900000, 5400000, 'VND', 40, 10, NULL, NULL, true, now(), now()),
-- Armani Code variants
('b1234567-89ab-4cde-f012-345678901130', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a58', 'ARMANI-CODE-50ML-EDP', 50.00, 'Spray Bottle', 2900000, 3200000, 'VND', 52, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901131', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a58', 'ARMANI-CODE-110ML-EDP', 110.00, 'Spray Bottle', 4600000, 5100000, 'VND', 35, 8, NULL, NULL, true, now(), now()),
-- Carolina Herrera Good Girl variants
('b1234567-89ab-4cde-f012-345678901132', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a59', 'CH-GOODGIRL-50ML-EDP', 50.00, 'Spray Bottle', 3100000, 3400000, 'VND', 56, 12, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901133', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a59', 'CH-GOODGIRL-80ML-EDP', 80.00, 'Spray Bottle', 4500000, 5000000, 'VND', 42, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901134', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a59', 'CH-GOODGIRL-150ML-EDP', 150.00, 'Spray Bottle', 7200000, 8000000, 'VND', 18, 5, NULL, NULL, true, now(), now()),
-- Carolina Herrera 212 VIP variants
('b1234567-89ab-4cde-f012-345678901135', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a60', 'CH-212VIP-50ML-EDT', 50.00, 'Spray Bottle', 2700000, 3000000, 'VND', 61, 14, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901136', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a60', 'CH-212VIP-80ML-EDT', 80.00, 'Spray Bottle', 4000000, 4400000, 'VND', 44, 10, NULL, NULL, true, now(), now());

-- Acqua di Parma Colonia variants
INSERT INTO public.product_variant (id, product_id, variant_sku, volume_ml, package_type, price, compare_at_price, currency_code, stock_quantity, low_stock_threshold, image_public_id, image_url, is_active, created_at, updated_at) VALUES
('b1234567-89ab-4cde-f012-345678901137', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a61', 'ADP-COLONIA-50ML-COL', 50.00, 'Cologne Bottle', 2800000, 3100000, 'VND', 48, 10, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901138', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a61', 'ADP-COLONIA-100ML-COL', 100.00, 'Cologne Bottle', 4200000, 4600000, 'VND', 35, 8, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901139', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a61', 'ADP-COLONIA-180ML-COL', 180.00, 'Cologne Bottle', 6500000, 7200000, 'VND', 18, 5, NULL, NULL, true, now(), now()),
-- Acqua di Parma Oud variants
('b1234567-89ab-4cde-f012-345678901140', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a62', 'ADP-OUD-100ML-EDP', 100.00, 'Spray Bottle', 5800000, 6400000, 'VND', 22, 5, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901141', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a62', 'ADP-OUD-180ML-EDP', 180.00, 'Spray Bottle', 9200000, 10100000, 'VND', 12, 3, NULL, NULL, true, now(), now()),
-- PDM Layton variants
('b1234567-89ab-4cde-f012-345678901142', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a63', 'PDM-LAYTON-75ML-EDP', 75.00, 'Spray Bottle', 7500000, 8300000, 'VND', 28, 6, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901143', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a63', 'PDM-LAYTON-125ML-EDP', 125.00, 'Spray Bottle', 10800000, 11900000, 'VND', 16, 4, NULL, NULL, true, now(), now()),
-- PDM Delina variants
('b1234567-89ab-4cde-f012-345678901144', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a64', 'PDM-DELINA-75ML-EDP', 75.00, 'Spray Bottle', 7300000, 8000000, 'VND', 32, 7, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901145', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a64', 'PDM-DELINA-125ML-EDP', 125.00, 'Spray Bottle', 10500000, 11600000, 'VND', 18, 4, NULL, NULL, true, now(), now()),
-- PDM Pegasus variants
('b1234567-89ab-4cde-f012-345678901146', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a65', 'PDM-PEGASUS-75ML-EDP', 75.00, 'Spray Bottle', 7400000, 8100000, 'VND', 30, 6, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901147', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a65', 'PDM-PEGASUS-125ML-EDP', 125.00, 'Spray Bottle', 10600000, 11700000, 'VND', 17, 4, NULL, NULL, true, now(), now()),
-- Temp Tester variants (for payment QA)
('b1234567-89ab-4cde-f012-345678901148', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'TEMP-5ML-MINI', 5.00, 'Mini Spray', 5000, NULL, 'VND', 150, 20, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901149', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'TEMP-10ML-MINI', 10.00, 'Mini Spray', 10000, NULL, 'VND', 120, 15, NULL, NULL, true, now(), now()),
('b1234567-89ab-4cde-f012-345678901150', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'TEMP-20ML-EDP', 20.00, 'Mini Spray', 20000, NULL, 'VND', 80, 10, NULL, NULL, true, now(), now());
