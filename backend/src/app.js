const path = require('path');
const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const { notFound, errorHandler } = require('./middleware/errorMiddleware');

const authRoutes = require('./routes/authRoutes');
const userRoutes = require('./routes/userRoutes');
const materialRoutes = require('./routes/materialRoutes');
const priceRoutes = require('./routes/priceRoutes');
const lotRoutes = require('./routes/lotRoutes');
const recyclerRoutes = require('./routes/recyclerRoutes');
const offerRoutes = require('./routes/offerRoutes');
const transactionRoutes = require('./routes/transactionRoutes');
const handoverRoutes = require('./routes/handoverRoutes');
const paymentRoutes = require('./routes/paymentRoutes');
const dashboardRoutes = require('./routes/dashboardRoutes');
const notificationRoutes = require('./routes/notificationRoutes');
const traceRoutes = require('./routes/traceRoutes');

const app = express();

const allowed = (process.env.CLIENT_ORIGIN || '*')
  .split(',')
  .map((s) => s.trim())
  .filter(Boolean);

app.use(
  cors({
    origin: (origin, callback) => {
      if (!origin || allowed.includes('*') || allowed.includes(origin)) {
        return callback(null, true);
      }
      return callback(null, true);
    },
    credentials: true,
  })
);
app.use(morgan('dev'));
app.use(express.json({ limit: '5mb' }));
app.use(express.urlencoded({ extended: true }));
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));
app.use(express.static(path.join(__dirname, '../public')));

app.get('/api/health', (req, res) => {
  res.json({
    success: true,
    message: 'Kabadiwala Connect API is running',
    data: { service: 'kabadiwala-connect', time: new Date().toISOString() },
  });
});

app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/materials', materialRoutes);
app.use('/api/prices', priceRoutes);
app.use('/api/lots', lotRoutes);
app.use('/api/recyclers', recyclerRoutes);
app.use('/api/offers', offerRoutes);
app.use('/api/transactions', transactionRoutes);
app.use('/api/handovers', handoverRoutes);
app.use('/api/payments', paymentRoutes);
app.use('/api/dashboard', dashboardRoutes);
app.use('/api/notifications', notificationRoutes);
app.use('/api', traceRoutes);

app.use(notFound);
app.use(errorHandler);

module.exports = app;
