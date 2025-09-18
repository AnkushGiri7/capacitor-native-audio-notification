export interface NativeAudioNotificationPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
