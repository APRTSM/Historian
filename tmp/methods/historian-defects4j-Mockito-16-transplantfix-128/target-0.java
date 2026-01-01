    public <T> T mock(Class<T> classToMock, MockSettings mockSettings, boolean shouldResetOngoingStubbing) { return mockUtil.createMock(classToMock, (MockSettingsImpl) mockSettings);
 }
