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