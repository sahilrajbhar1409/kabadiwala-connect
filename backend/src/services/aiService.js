<<<<<<< HEAD
const { ApiError } = require('../middleware/errorMiddleware');

const getConfig = () => ({
  url: (process.env.AI_SERVICE_URL || '').replace(/\/$/, ''),
  timeoutMs: Number(process.env.AI_SERVICE_TIMEOUT_MS || 15000),
});

const analyzeScrap = async ({ imageUrl, weightKg, actualPrice, benchmarkRate }) => {
  const { url, timeoutMs } = getConfig();
  if (!url) throw new ApiError(503, 'AI service is not configured');
  if (!imageUrl) throw new ApiError(400, 'An image URL is required for AI analysis');
  let parsedImageUrl;
  try {
    parsedImageUrl = new URL(imageUrl);
  } catch (_error) {
    throw new ApiError(400, 'imageUrl must be a valid URL');
  }
  if (!['http:', 'https:'].includes(parsedImageUrl.protocol)) {
    throw new ApiError(400, 'imageUrl must use HTTP or HTTPS');
  }

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);

  try {
    const imageResponse = await fetch(parsedImageUrl, { signal: controller.signal });
    if (!imageResponse.ok) {
      throw new ApiError(502, `AI image fetch failed with status ${imageResponse.status}`);
    }

    const imageBuffer = Buffer.from(await imageResponse.arrayBuffer());
    const form = new FormData();
    form.append('image', new Blob([imageBuffer], {
      type: imageResponse.headers.get('content-type') || 'image/jpeg',
    }), 'lot-image.jpg');
    form.append('weight_kg', String(weightKg));
    form.append('actual_price', String(actualPrice));
    form.append('benchmark_rate_per_kg', String(benchmarkRate));

    const response = await fetch(`${url}/analyze-scrap`, {
      method: 'POST',
      body: form,
      signal: controller.signal,
    });
    const payload = await response.json().catch(() => null);
    if (!response.ok || !payload?.classification || !payload?.valuation || !payload?.fraud_audit) {
      throw new ApiError(502, payload?.detail || 'AI service returned an invalid response');
    }

    return payload;
  } catch (error) {
    if (error.name === 'AbortError') throw new ApiError(504, 'AI service request timed out');
    if (error.statusCode) throw error;
    throw new ApiError(502, `AI service request failed: ${error.message}`);
  } finally {
    clearTimeout(timer);
  }
};

module.exports = { analyzeScrap };
=======
const { Blob, FormData } = global;

const analyzeLot = async ({ photoUrl, weight, actualPrice }) => {
  const baseUrl = process.env.AI_SERVICE_URL;
  if (!baseUrl || !photoUrl) return null;

  const imageResponse = await fetch(photoUrl);
  if (!imageResponse.ok) throw new Error(`AI image fetch failed: ${imageResponse.status}`);

  const form = new FormData();
  form.append('image', new Blob([await imageResponse.arrayBuffer()], {
    type: imageResponse.headers.get('content-type') || 'image/jpeg',
  }), 'lot-photo.jpg');
  form.append('weight_kg', String(weight));
  form.append('actual_price', String(actualPrice));

  const response = await fetch(`${baseUrl.replace(/\/$/, '')}/analyze-scrap`, {
    method: 'POST',
    body: form,
  });
  if (!response.ok) throw new Error(`AI analysis failed: ${response.status}`);
  return response.json();
};

module.exports = { analyzeLot };
>>>>>>> f8f13893033af90e6bddcbdb82ab63c12f831ffd
