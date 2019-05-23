/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {Component} from 'react';
import {
    StyleSheet,
    Text,
    View,
    Platform,
    ScrollView,
    ImageBackground,
    SafeAreaView,
    Alert
} from 'react-native';
import MappButton from './src/components/MappButton'
import MappInputText from './src/components/MappInputText'

import {MappEventEmitter, Mapp} from './src/js'

const instructions = Platform.select({
    ios: 'Press Cmd+R to reload,\n' + 'Cmd+D or shake for dev menu',
    android:
    'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});
/**
 * Created by Aleksandar Marinkovic on 5/16/19.
 * Copyright (c) 2019 MAPP.
 */
type Props = {};
export default class App extends Component<Props> {
    state = {
        aliasState: '',
        removeAttribute: '',
        addAttribute: '',
        getAttribute: '',
        removeTags: '',
        setTags: '',
        defaultError: false,
        defaultErrorMessage: '',
    }

    constructor (props) {
        super(props);


    }

    handleTextChange = (type) => (text) => {
        this.setState({[type]: text});
    }

    render() {
        const {state} = this;
        return (
            <SafeAreaView>
                <ScrollView>
                    <ImageBackground style={{width: "100%"}}
                                     source={require("./src/img/promo_image.png")}
                                     resizeMode={"contain"}
                                     backgroundColor={"black"}
                    >
                        <View style={styles.container}>


                            <Text style={styles.welcome}>Welcome to React Native Mapp plugin!</Text>
                            <MappInputText
                                maxLength={255}
                                label="Set Device Alias"
                                autoCorrect={false}
                                autoCapitalize="none"
                                value={state.aliasState}
                                onChangeText={this.handleTextChange('aliasState')}
                                error={state.defaultError}
                                errorMessage={state.defaultErrorMessage}
                            />
                            <MappButton
                                text={"Set Device Alias"}
                                onPress={this.setAlias}
                            />
                            <MappButton
                                text={"Get Device Alias"}
                                onPress={this.getAlias}/>
                            <MappButton
                                text={"Device Information"}
                                onPress={this.getDevice}/>
                            <MappButton
                                text={"Is Push Enabled"}
                                onPress={this.isPushEnabled}/>
                            <MappButton
                                text={"Opt in"}
                                onPress={this.optIn}/>
                            <MappButton
                                text={"Opt out"}
                                onPress={this.optOut}/>
                            <MappButton
                                text={"Start Geo"}
                                onPress={this.startGeo}/>
                            <MappButton
                                text={"Stop Geo"}
                                onPress={this.stopGeo}/>
                            <MappButton
                                text={"Fetch inbox messages"}
                                onPress={this.fetchInbox}/>
                            <MappButton
                                text={"In App: App Open"}
                                onPress={this.appOpenEvent}/>
                            <MappButton
                                text={"In App: App Feedback"}
                                onPress={this.appFeedbackEvent}/>
                            <MappButton
                                text={"In App: App Discount"}
                                onPress={this.appDiscountEvent}/>
                            <MappButton
                                text={"In App: App Promo"}
                                onPress={this.appPromoEvent}/>
                            <MappButton
                                text={"Fetch Multiple Messages"}
                                onPress={this.fetchMultipleMessages}/>
                            <MappButton
                                text={"Get Tags"}
                                onPress={this.getTags}/>

                            <MappInputText
                                maxLength={255}
                                label="Set Tags"
                                autoCorrect={false}
                                autoCapitalize="none"
                                value={state.setTags}
                                onChangeText={this.handleTextChange('setTags')}
                                error={state.defaultError}
                                errorMessage={state.defaultErrorMessage}
                            />
                            <MappButton
                                text={"Set Tags"}
                                onPress={this.setTagsEvent}/>

                            <MappInputText
                                maxLength={255}
                                label="Remove Tags"
                                autoCorrect={false}
                                autoCapitalize="none"
                                value={state.removeTags}
                                onChangeText={this.handleTextChange('removeTags')}
                                error={state.defaultError}
                                errorMessage={state.defaultErrorMessage}
                            />
                            <MappButton
                                text={"Remove Tag"}
                                onPress={this.removeTagEvent}/>
                            <MappInputText
                                maxLength={255}
                                label="Set Attribute"
                                autoCorrect={false}
                                autoCapitalize="none"
                                value={state.addAttribute}
                                onChangeText={this.handleTextChange('addAttribute')}
                                error={state.defaultError}
                                errorMessage={state.defaultErrorMessage}
                            />
                            <MappButton
                                text={"Set Attribute"}
                                onPress={this.setAttributeEvent}/>
                            <MappInputText
                                maxLength={255}
                                label="Get Attribute"
                                autoCorrect={false}
                                autoCapitalize="none"
                                value={state.getAttribute}
                                onChangeText={this.handleTextChange('getAttribute')}
                                error={state.defaultError}
                                errorMessage={state.defaultErrorMessage}
                            />
                            <MappButton
                                text={"Get Attribute"}
                                onPress={this.getAttributeEvent}/>
                            <MappInputText
                                maxLength={255}
                                label="Remove Attribute"
                                autoCorrect={false}
                                autoCapitalize="none"
                                value={state.removeAttribute}
                                onChangeText={this.handleTextChange('removeAttribute')}
                                error={state.defaultError}
                                errorMessage={state.defaultErrorMessage}
                            />
                            <MappButton
                                text={"Remove Attribute"}
                                onPress={this.removeAttributeEvent}/>
                            <MappButton
                                text={"Remove Badge Number"}
                                onPress={this.removeBadgeNumberEvent}/>
                            <MappButton
                                text={"Lock Orientation"}
                                onPress={this.lockOrientationEvent}/>
                            <MappButton
                                text={"Engage"}
                                onPress={this.engageEvent}/>

                        </View>
                    </ImageBackground>
                </ScrollView>
            </SafeAreaView>
        );
    }

    setAlias = () => {
        Mapp.setAlias(this.state.aliasState)
    };

    getAlias = () => {
        Mapp.getAlias().then(data => {
            console.log(data);
            Alert.alert(data)
        });

    };

    isPushEnabled = () => {
        Mapp.isPushEnabled().then(data => {
            console.log(data);
            Alert.alert(data.toString())
        });

    };



    getDevice = () => {
        Mapp.getDeviceInfo().then(data => {
            Alert.alert(JSON.stringify(data))
        });
    };

    optIn = () => {
        Mapp.setPushEnabled(true)
    };

    optOut = () => {
        Mapp.setPushEnabled(false)
    };
    startGeo = () => {
        Mapp.startGeoFencing()
    };
    stopGeo = () => {
        Mapp.stopGeoFencing()
    };
    fetchInbox = () => {
        Mapp.fetchInboxMessage().then(data => {
            Alert.alert(JSON.stringify(data))
        });
    };
    appOpenEvent = () => {
        Mapp.triggerInApp("app_open")
    };
    appFeedbackEvent = () => {
        Mapp.triggerInApp("app_feedback")
    };
    appDiscountEvent = () => {
        Mapp.triggerInApp("app_discount")
    };
    appPromoEvent = () => {
        Mapp.triggerInApp("app_promo")
    };
    fetchMultipleMessages = () => {

        Mapp.fetchInboxMessage().then(data => {
            Alert.alert(JSON.stringify(data))
        });
    };

    getTags = () => {
        Mapp.getTags().then(data => {
            Alert.alert(JSON.stringify(data))
        });
    };

    setTagsEvent = () => {
        Mapp.addTag(this.state.setTags)
    };

    removeTagEvent = () => {
        Mapp.removeTag(this.state.removeTags)
    };
    setAttributeEvent = () => {
        Mapp.setAttributeString("test",this.state.addAttribute)
    };
    getAttributeEvent = () => {
        Mapp.getAttributeStringValue("test").then(data => {
            Alert.alert(data)
        });
    };
    removeAttributeEvent = () => {
        Mapp.removeAttribute("test");
    };
    removeBadgeNumberEvent = () => {
        Mapp.removeBadgeNumber();
    };
    lockOrientationEvent = () => {
       // Mapp.engage("5c59a56fd39ce9.45048743","1028993954364","https://jamie-test.shortest-route.com","263176","55")
    };

    engageEvent = () => {
        Mapp.engage("5c59a56fd39ce9.45048743","1028993954364","https://jamie-test.shortest-route.com","263176","55")
    };

}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        marginHorizontal: 20,
        justifyContent: 'center',
        alignItems: 'center',

    },
    welcome: {
        fontSize: 20,
        color: "white",
        textAlign: 'center',
        margin: 10,
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5,
    },
});
