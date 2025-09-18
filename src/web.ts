import { WebPlugin } from '@capacitor/core';

import type { NativeAudioNotificationPlugin } from './definitions';

export class NativeAudioNotificationWeb extends WebPlugin implements NativeAudioNotificationPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
