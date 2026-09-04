const Handover = require('../models/Handover');
const Lot = require('../models/Lot');
const Transaction = require('../models/Transaction');
const generateReferenceId = require('../utils/generateReferenceId');
const { ApiError } = require('../middleware/errorMiddleware');
const { notify } = require('../utils/notify');

const refreshVerification = (handover) => {
  if (handover.collectorConfirmation && handover.recyclerConfirmation) {
    handover.verificationStatus = 'VERIFIED';
  } else if (handover.collectorConfirmation || handover.recyclerConfirmation) {
    handover.verificationStatus = 'PARTIAL';
  } else {
    handover.verificationStatus = 'PENDING';
  }
};

const createHandover = async ({ lot, transaction, weight, photos, location, actorRole }) => {
  if (!['OFFER_ACCEPTED', 'SCHEDULED', 'HANDED_OVER'].includes(lot.status)) {
    throw new ApiError(400, 'Handover can only start after an offer is accepted');
  }

  let handover = await Handover.findOne({ transaction: transaction._id });
  if (!handover) {
    handover = await Handover.create({
      handoverReference: generateReferenceId('handover'),
      lot: lot._id,
      transaction: transaction._id,
      materialPhotos: photos || [],
      weight,
      location: location || {},
      collectorConfirmation: actorRole === 'collector',
      recyclerConfirmation: actorRole === 'recycler',
    });
  } else {
    handover.weight = weight ?? handover.weight;
    if (photos?.length) handover.materialPhotos = [...handover.materialPhotos, ...photos];
    if (location) handover.location = location;
    if (actorRole === 'collector') handover.collectorConfirmation = true;
    if (actorRole === 'recycler') handover.recyclerConfirmation = true;
  }

  refreshVerification(handover);
  await handover.save();

  lot.status = handover.verificationStatus === 'VERIFIED' ? 'HANDED_OVER' : 'SCHEDULED';
  await lot.save();

  transaction.status = handover.verificationStatus === 'VERIFIED' ? 'HANDED_OVER' : 'IN_PROGRESS';
  await transaction.save();

  if (handover.verificationStatus === 'VERIFIED') {
    await notify({
      user: lot.collector,
      title: 'Handover verified',
      message: `Handover ${handover.handoverReference} is fully verified.`,
      type: 'HANDOVER_VERIFIED',
      relatedEntityId: handover._id.toString(),
    });
    await notify({
      user: transaction.recycler,
      title: 'Handover verified',
      message: `Handover ${handover.handoverReference} is fully verified.`,
      type: 'HANDOVER_VERIFIED',
      relatedEntityId: handover._id.toString(),
    });
  }

  return handover;
};

const confirmHandover = async (handover, role) => {
  if (role === 'collector') handover.collectorConfirmation = true;
  if (role === 'recycler') handover.recyclerConfirmation = true;
  if (role === 'admin') {
    handover.collectorConfirmation = true;
    handover.recyclerConfirmation = true;
  }
  refreshVerification(handover);
  await handover.save();

  if (handover.verificationStatus === 'VERIFIED') {
    await Lot.findByIdAndUpdate(handover.lot, { status: 'HANDED_OVER' });
    await Transaction.findByIdAndUpdate(handover.transaction, { status: 'HANDED_OVER' });
    await notify({
      user: (await Transaction.findById(handover.transaction)).collector,
      title: 'Handover verified',
      message: `Handover ${handover.handoverReference} is fully verified.`,
      type: 'HANDOVER_VERIFIED',
      relatedEntityId: handover._id.toString(),
    });
  }

  return handover;
};

module.exports = { createHandover, confirmHandover };
