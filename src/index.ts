import { registerPlugin } from '@capacitor/core';

import type { NativeAudioNotificationPlugin } from './definitions';

const NativeAudioNotification = registerPlugin<NativeAudioNotificationPlugin>('NativeAudioNotification', {
  web: () => import('./web').then((m) => new m.NativeAudioNotificationWeb()),
});

export * from './definitions';
export { NativeAudioNotification };
