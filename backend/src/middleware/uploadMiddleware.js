const fs = require('fs');
const path = require('path');
const multer = require('multer');
const { cloudinary, isCloudinaryConfigured, configureCloudinary } = require('../config/cloudinary');
const { ApiError } = require('./errorMiddleware');

configureCloudinary();

const uploadsDir = path.join(__dirname, '../../uploads');
if (!fs.existsSync(uploadsDir)) {
  fs.mkdirSync(uploadsDir, { recursive: true });
}

const storage = multer.memoryStorage();

const fileFilter = (req, file, cb) => {
  if (!file.mimetype.startsWith('image/')) {
    return cb(new ApiError(400, 'Only image uploads are allowed'), false);
  }
  cb(null, true);
};

const upload = multer({
  storage,
  fileFilter,
  limits: { fileSize: 8 * 1024 * 1024, files: 6 },
});

const persistImageBuffer = async (file) => {
  if (isCloudinaryConfigured()) {
    const folder = process.env.CLOUDINARY_UPLOAD_FOLDER || 'kabadiwala-connect';
    const uploaded = await new Promise((resolve, reject) => {
      const stream = cloudinary.uploader.upload_stream(
        { folder },
        (error, result) => {
          if (error) reject(error);
          else resolve(result);
        }
      );
      stream.end(file.buffer);
    });
    return uploaded.secure_url;
  }

  const ext = path.extname(file.originalname || '') || '.jpg';
  const filename = `${Date.now()}-${Math.round(Math.random() * 1e9)}${ext}`;
  fs.writeFileSync(path.join(uploadsDir, filename), file.buffer);
  return `/uploads/${filename}`;
};

const persistUploadedFiles = async (files = []) => {
  const urls = [];
  for (const file of files) {
    urls.push(await persistImageBuffer(file));
  }
  return urls;
};

module.exports = { upload, persistUploadedFiles, persistImageBuffer };
