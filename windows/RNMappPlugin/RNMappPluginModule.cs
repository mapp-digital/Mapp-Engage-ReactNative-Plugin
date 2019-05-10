using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Mapp.Plugin.RNMappPlugin
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNMappPluginModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNMappPluginModule"/>.
        /// </summary>
        internal RNMappPluginModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNMappPlugin";
            }
        }
    }
}
