import { WebPlugin } from '@capacitor/core';
import type { NativeAudioNotificationPlugin } from './definitions';

export class NativeAudioNotificationWeb extends WebPlugin implements NativeAudioNotificationPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async configure(): Promise<void> {
    console.log('configure called');
  }

  async testNotification(options: {
    amount: string;
    currency?: string;
    customerName?: string;
    transactionId?: string;
    timestamp?: number;
  }): Promise<void> {
    console.log('testNotification', options);
  }

  async setMerchantInfo(options: { businessName: string }): Promise<{ businessName: string }> {
    console.log('setMerchantInfo', options);
    return { businessName: options.businessName };
  }

  async toggleNotifications(options: { enabled?: boolean }): Promise<{ enabled: boolean }> {
    console.log('toggleNotifications', options);
    return { enabled: options.enabled ?? true };
  }

  async playTts(options: { text?: string }): Promise<void> {
    console.log('playTts', options);
  }

  async stopTts(): Promise<void> {
    console.log('stopTts called');
  }
}