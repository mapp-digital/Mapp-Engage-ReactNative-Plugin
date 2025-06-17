import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  exampleMethod(param: string): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('MappEngagePlugin');
