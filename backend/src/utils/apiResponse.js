const success = (res, { status = 200, message = 'Success', data = null } = {}) => {
  const payload = { success: true, message };
  if (data !== null) payload.data = data;
  return res.status(status).json(payload);
};

const fail = (res, { status = 400, message = 'Request failed', errors = undefined } = {}) => {
  const payload = { success: false, message };
  if (errors) payload.errors = errors;
  return res.status(status).json(payload);
};

module.exports = { success, fail };
